package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private final DataSource dataSource = DataSourceConfig.getInstance();

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        DatabasePopulatorUtils.execute(dataSource);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
            statement.execute("truncate table users restart identity");
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void commitsPasswordChangeAndHistoryTogether() {
        final var userService = transactionalService(new UserHistoryDao(jdbcTemplate));
        final var newPassword = "newPassword";
        final var createdBy = "editor";
        final var startedAt = LocalDateTime.now();

        userService.changePassword(1L, newPassword, createdBy);

        final var finishedAt = LocalDateTime.now();
        assertNoBoundConnection();
        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", newPassword, "hkkang@woowahan.com"));
        final var histories = jdbcTemplate.query("select * from user_history", resultSet ->
                new HistorySnapshot(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getObject("created_at", LocalDateTime.class),
                        resultSet.getString("created_by")
                ));
        assertThat(histories).singleElement().satisfies(history -> {
            assertThat(history.id()).isEqualTo(1L);
            assertThat(history.userId()).isEqualTo(1L);
            assertThat(history.account()).isEqualTo("gugu");
            assertThat(history.password()).isEqualTo(newPassword);
            assertThat(history.email()).isEqualTo("hkkang@woowahan.com");
            assertThat(history.createdBy()).isEqualTo(createdBy);
            assertThat(history.createdAt()).isBetween(startedAt.minusNanos(1_000), finishedAt.plusNanos(1_000));
        });
    }

    @Test
    void rollsBackPasswordWhenHistoryFailsBeforeInsert() {
        final var userService = transactionalService(new MockUserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "editor"))
                .isInstanceOf(DataAccessException.class);

        assertPasswordAndHistoryRolledBack();
    }

    @Test
    void rollsBackBothPasswordAndHistoryWhenFailureOccursAfterInsert() {
        final var failure = new DataAccessException("Failure after inserting history");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw failure;
            }
        };
        final var userService = transactionalService(userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "editor"))
                .isSameAs(failure);

        assertPasswordAndHistoryRolledBack();
    }

    @Test
    void rollsBackPasswordWhenHistoryViolatesDatabaseConstraint() {
        final var userService = transactionalService(new UserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertPasswordAndHistoryRolledBack();
    }

    @Test
    void reusesServiceAfterRollbackAndAcrossSuccessfulTransactions() {
        final var userService = transactionalService(new UserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "failed-password", null))
                .isInstanceOf(DataAccessException.class);
        assertPasswordAndHistoryRolledBack();

        userService.changePassword(1L, "first-password", "editor");
        assertNoBoundConnection();
        userService.changePassword(1L, "second-password", "editor");
        assertNoBoundConnection();

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("second-password");
        final var passwords = jdbcTemplate.query(
                "select password from user_history order by id", resultSet -> resultSet.getString("password"));
        assertThat(passwords).containsExactly("first-password", "second-password");
    }

    @Test
    void rollbackInOneThreadDoesNotAffectCommitInAnotherThread() throws Exception {
        userDao.insert(new User("eian", "original-password", "eian@woowahan.com"));
        final var barrier = new CyclicBarrier(2);
        final var failure = new DataAccessException("Failure in the first transaction");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();
                super.log(userHistory);
                awaitBothTransactions(barrier);
                if (userHistory.getUserId() == 1L) {
                    throw failure;
                }
            }
        };
        final var userService = transactionalService(userHistoryDao);

        try (final var executor = Executors.newFixedThreadPool(2)) {
            final var rolledBack = executor.submit(() -> {
                assertThatThrownBy(() -> userService.changePassword(1L, "failed-password", "gugu"))
                        .isSameAs(failure);
                assertNoBoundConnection();
            });
            final var committed = executor.submit(() -> {
                userService.changePassword(2L, "committed-password", "eian");
                assertNoBoundConnection();
            });
            rolledBack.get(10, TimeUnit.SECONDS);
            committed.get(10, TimeUnit.SECONDS);
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("committed-password");
        final var historyUserIds = jdbcTemplate.query(
                "select user_id from user_history", resultSet -> resultSet.getLong("user_id"));
        assertThat(historyUserIds).containsExactly(2L);
        assertNoBoundConnection();
    }

    private UserService transactionalService(final UserHistoryDao userHistoryDao) {
        return new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));
    }

    private void assertNoBoundConnection() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    private static void awaitBothTransactions(final CyclicBarrier barrier) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while synchronizing transactions", e);
        } catch (BrokenBarrierException | TimeoutException e) {
            throw new IllegalStateException("Transactions did not reach the synchronization point", e);
        }
    }

    private void assertPasswordAndHistoryRolledBack() {
        assertNoBoundConnection();
        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    private record HistorySnapshot(long id, long userId, String account, String password, String email,
                                   LocalDateTime createdAt, String createdBy) {
    }
}

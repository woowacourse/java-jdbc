package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-service-test;DB_CLOSE_DELAY=-1");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("truncate table users restart identity");
        jdbcTemplate.update("truncate table user_history restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void verifyTransactionReleased() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    @Test
    void testChangePassword() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var beforeChange = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(1L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo(newPassword);
            assertThat(resultSet.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(resultSet.getString("created_by")).isEqualTo(createBy);
            assertThat(resultSet.getTimestamp("created_at").toLocalDateTime())
                    .isBetween(beforeChange, LocalDateTime.now());
            assertThat(resultSet.next()).isFalse();
        }
    }

    @Test
    void testTransactionRollback() {
        assertThat(dataSource).isNotSameAs(DataSourceConfig.getInstance());
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history",
                resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    @Test
    void sqlFailureRollsBackPasswordChange() {
        final var userService = new TxUserService(
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)), dataSource);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history",
                resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    @Test
    void failedChangePreservesEarlierHistoryAndCanBeRetried() {
        final var userService = new TxUserService(
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)), dataSource);
        userService.changePassword(1L, "firstPassword", "gugu");
        final var failingService = new TxUserService(
                new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate)), dataSource);

        assertThrows(DataAccessException.class,
                () -> failingService.changePassword(1L, "failedPassword", "gugu"));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("firstPassword");
        final var passwords = jdbcTemplate.query("select password from user_history order by id",
                resultSet -> resultSet.getString(1));
        assertThat(passwords).containsExactly("firstPassword");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        userService.changePassword(1L, "retryPassword", "gugu");

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("retryPassword");
        final var afterRetry = jdbcTemplate.query("select password from user_history order by id",
                resultSet -> resultSet.getString(1));
        assertThat(afterRetry).containsExactly("firstPassword", "retryPassword");
    }

    @Test
    void changesRemainInvisibleToAnotherConnectionUntilCommit() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory history) {
                super.log(history);
                assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
                try (final var observer = dataSource.getConnection();
                     final var statement = observer.createStatement()) {
                    try (final var resultSet = statement.executeQuery("select password from users where id = 1")) {
                        assertThat(resultSet.next()).isTrue();
                        assertThat(resultSet.getString(1)).isEqualTo("password");
                    }
                    try (final var resultSet = statement.executeQuery("select count(*) from user_history")) {
                        assertThat(resultSet.next()).isTrue();
                        assertThat(resultSet.getLong(1)).isZero();
                    }
                } catch (SQLException e) {
                    throw new DataAccessException(e);
                }
            }
        };
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        userService.changePassword(1L, "newPassword", "gugu");

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history",
                resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isEqualTo(1L);
    }
}

package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("drop all objects");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void logUsingDataSourceStoresUserSnapshot() {
        final var user = userDao.findById(1L);
        final var history = new UserHistory(user, "support");

        new UserHistoryDao(DataSourceConfig.getInstance()).log(history);

        final var actual = findHistory(user.getId());
        assertThat(actual.userId()).isEqualTo(user.getId());
        assertThat(actual.account()).isEqualTo(user.getAccount());
        assertThat(actual.password()).isEqualTo(user.getPassword());
        assertThat(actual.email()).isEqualTo(user.getEmail());
        assertThat(actual.createdAt()).isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MILLIS));
        assertThat(actual.createdBy()).isEqualTo("support");
        assertThat(countHistoryRows()).isEqualTo(1L);
    }

    @Test
    void logWithSynchronizedConnectionCommitsAlongWithPasswordChange() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(DataSourceConfig.getInstance(), connection);
            try {
                final var user = userDao.findById(1L);
                user.changePassword("changed-password");
                userDao.update(user);

                userHistoryDao.log(new UserHistory(user, "support"));

                assertThat(countHistoryRows()).isEqualTo(1L);
                assertThat(userDao.findById(1L).getPassword()).isEqualTo("changed-password");
                try (final var independentConnection = DataSourceConfig.getInstance().getConnection()) {
                    final var committedCount = jdbcTemplate.queryForObject(independentConnection,
                            "select count(*) from user_history", row -> row.getLong(1));
                    assertThat(committedCount).isZero();
                    final var committedPassword = jdbcTemplate.queryForObject(independentConnection,
                            "select password from users where id = ?", row -> row.getString("password"), 1L);
                    assertThat(committedPassword).isEqualTo("password");
                }
                connection.commit();
            } finally {
                TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
                connection.rollback();
            }
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("changed-password");
        assertThat(countHistoryRows()).isEqualTo(1L);
        assertThat(findHistory(1L).password()).isEqualTo("changed-password");
        assertThat(findHistory(1L).createdBy()).isEqualTo("support");
    }

    @Test
    void logWithSynchronizedConnectionRollsBackAlongWithPasswordChange() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(DataSourceConfig.getInstance(), connection);
            try {
                final var user = userDao.findById(1L);
                user.changePassword("changed-password");
                userDao.update(user);

                userHistoryDao.log(new UserHistory(user, "support"));

                final var historyCount = countHistoryRows();
                assertThat(historyCount).isEqualTo(1L);
                assertThat(userDao.findById(1L).getPassword()).isEqualTo("changed-password");
            } finally {
                TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
                connection.rollback();
            }
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(countHistoryRows()).isZero();
    }

    @Test
    void logFailureLeavesConnectionAvailableForRollback() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(DataSourceConfig.getInstance(), connection);
            try {
                final var user = userDao.findById(1L);
                user.changePassword("changed-password");
                userDao.update(user);
                final var history = new UserHistory(user, "g".repeat(101));

                assertThatThrownBy(() -> userHistoryDao.log(history))
                        .isInstanceOf(DataAccessException.class)
                        .hasCauseInstanceOf(SQLException.class);
                assertThat(connection.isClosed()).isFalse();
            } finally {
                TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
                connection.rollback();
            }
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(countHistoryRows()).isZero();
    }

    private long countHistoryRows() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1));
    }

    private HistorySnapshot findHistory(final long userId) {
        final var sql = "select user_id, account, password, email, created_at, created_by from user_history where user_id = ?";
        return jdbcTemplate.queryForObject(sql, row -> new HistorySnapshot(
                row.getLong("user_id"),
                row.getString("account"),
                row.getString("password"),
                row.getString("email"),
                row.getObject("created_at", LocalDateTime.class),
                row.getString("created_by")), userId);
    }

    private record HistorySnapshot(long userId, String account, String password, String email,
                                   LocalDateTime createdAt, String createdBy) {
    }
}

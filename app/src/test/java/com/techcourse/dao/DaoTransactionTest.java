package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DaoTransactionTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private Long userId;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
        userId = userDao.findByAccount("gugu").getId();
    }

    @Test
    void boundConnectionRollsBackDaoCallsWithoutConnectionArguments() throws SQLException {
        final var source = DataSourceConfig.getInstance();
        final var connection = DataSourceUtils.getConnection(source);
        try (connection) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(source, connection);
            try {
                final var user = userDao.findById(userId);
                user.changePassword("new-password");
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, "admin"));
                assertThat(connection.isClosed()).isFalse();
                assertThat(userDao.findById(userId).getPassword()).isEqualTo("new-password");
                assertThat(jdbcTemplate.query("select password from user_history", rs -> rs.getString("password")))
                        .containsExactly("new-password");
            } finally {
                try {
                    connection.rollback();
                } finally {
                    TransactionSynchronizationManager.unbindResource(source);
                }
            }
        }
        assertThat(userDao.findById(userId).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"))).isEmpty();
        assertThat(TransactionSynchronizationManager.getResource(source)).isNull();
    }

    @Test
    void sharedConnectionCommitsPasswordAndHistoryTogether() throws SQLException {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(DataSourceConfig.getInstance(), connection);
            try {
                changePasswordAndSaveHistory(connection);
                connection.commit();
            } finally {
                try {
                    connection.rollback();
                } finally {
                    TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
                }
            }
        }

        assertThat(userDao.findById(userId).getPassword()).isEqualTo("new-password");
        assertThat(jdbcTemplate.query("select password from user_history", rs -> rs.getString("password")))
                .containsExactly("new-password");
    }

    @Test
    void sharedConnectionRollsBackPasswordAndHistoryTogether() throws SQLException {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(DataSourceConfig.getInstance(), connection);
            try {
                changePasswordAndSaveHistory(connection);
            } finally {
                try {
                    connection.rollback();
                } finally {
                    TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
                }
            }
        }

        assertThat(userDao.findById(userId).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"))).isEmpty();
    }

    private void changePasswordAndSaveHistory(final Connection connection) throws SQLException {
        final var user = userDao.findById(userId);
        user.changePassword("new-password");
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, "admin"));

        assertThat(connection.isClosed()).isFalse();
        assertThat(userDao.findById(userId).getPassword()).isEqualTo("new-password");
        assertThat(jdbcTemplate.query(connection, "select password from user_history",
                rs -> rs.getString("password"))).containsExactly("new-password");
        // 별도 연결은 현재 트랜잭션의 미커밋 변경을 볼 수 없다.
        try (var observer = DataSourceConfig.getInstance().getConnection()) {
            assertThat(jdbcTemplate.<String>queryForObject(observer,
                    "select password from users where id = ?", rs -> rs.getString("password"), userId))
                    .isEqualTo("password");
            assertThat(jdbcTemplate.query(observer, "select id from user_history", rs -> rs.getLong("id")))
                    .isEmpty();
        }
    }
}

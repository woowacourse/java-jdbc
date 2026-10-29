package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
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
    void sharedConnectionCommitsPasswordAndHistoryTogether() throws SQLException {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                changePasswordAndSaveHistory(connection);
                connection.commit();
            } finally {
                connection.rollback();
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
            try {
                changePasswordAndSaveHistory(connection);
            } finally {
                connection.rollback();
            }
        }

        assertThat(userDao.findById(userId).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"))).isEmpty();
    }

    private void changePasswordAndSaveHistory(final Connection connection) throws SQLException {
        final var user = userDao.findById(connection, userId);
        user.changePassword("new-password");
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, "admin"));

        assertThat(connection.isClosed()).isFalse();
        assertThat(userDao.findById(connection, userId).getPassword()).isEqualTo("new-password");
        assertThat(jdbcTemplate.query(connection, "select password from user_history",
                rs -> rs.getString("password"))).containsExactly("new-password");
        assertThat(userDao.findById(userId).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"))).isEmpty();
    }
}

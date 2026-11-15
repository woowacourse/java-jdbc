package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    @Test
    void log() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userHistory = new UserHistory(null, 1L, "gugu", "newPassword", "hkkang@woowahan.com", "gugu");

        userHistoryDao.log(userHistory);

        final var actual = jdbcTemplate.queryForObject("select * from user_history where user_id = ?", rs -> {
            assertThat(rs.getLong("user_id")).isEqualTo(userHistory.getUserId());
            assertThat(rs.getString("account")).isEqualTo(userHistory.getAccount());
            assertThat(rs.getString("password")).isEqualTo(userHistory.getPassword());
            assertThat(rs.getString("email")).isEqualTo(userHistory.getEmail());
            assertThat(rs.getTimestamp("created_at").toLocalDateTime())
                    .isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(rs.getString("created_by")).isEqualTo(userHistory.getCreateBy());
            return rs.getLong("id");
        }, userHistory.getUserId());

        assertThat(actual).isPositive();
    }

    @Test
    void rollbackUserAndHistoryWithSameConnection() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table users restart identity");
        jdbcTemplate.update("truncate table user_history restart identity");
        final var userDao = new UserDao(jdbcTemplate);
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            final var user = userDao.findById(connection, 1L);
            user.changePassword("newPassword");

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, "gugu"));

            assertThat(userDao.findById(connection, 1L).getPassword()).isEqualTo("newPassword");
            final var historyPassword = jdbcTemplate.queryForObject(connection,
                    "select password from user_history where user_id = ?", rs -> rs.getString("password"), 1L);
            assertThat(historyPassword).isEqualTo("newPassword");
            connection.rollback();
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isZero();
    }
}

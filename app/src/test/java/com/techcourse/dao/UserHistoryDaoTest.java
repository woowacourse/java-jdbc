package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
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
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE user_history RESTART IDENTITY");
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() {
        final var userHistory = new UserHistory(null, 1L, "gugu", "password", "hkkang@woowahan.com", "admin");

        userHistoryDao.log(userHistory);

        final var sql = "select user_id, account, password, email, created_at, created_by " +
                "from user_history " +
                "where id = ?";
        final var actual = jdbcTemplate.queryForObject(
                sql,
                rs -> new HistoryRow(
                        rs.getLong("user_id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getObject("created_at", LocalDateTime.class),
                        rs.getString("created_by")
                ),
                1L
        );

        assertThat(actual.userId()).isEqualTo(1L);
        assertThat(actual.account()).isEqualTo("gugu");
        assertThat(actual.password()).isEqualTo("password");
        assertThat(actual.email()).isEqualTo("hkkang@woowahan.com");
        assertThat(actual.createdAt()).isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.SECONDS));
        assertThat(actual.createdBy()).isEqualTo("admin");
    }

    @Test
    void logThrowsDataAccessExceptionWhenInsertFails() {
        final var invalidHistory = new UserHistory(null, 1L, "gugu", "password", "hkkang@woowahan.com", null);

        assertThatThrownBy(() -> userHistoryDao.log(invalidHistory))
                .isInstanceOf(DataAccessException.class);
    }

    private record HistoryRow(
            long userId,
            String account,
            String password,
            String email,
            LocalDateTime createdAt,
            String createdBy
    ) {
    }
}

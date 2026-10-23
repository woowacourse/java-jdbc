package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @BeforeEach
    void setUp() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
        }
    }

    @Test
    void logsHistoryWithJdbcTemplate() throws SQLException {
        assertHistorySaved(new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance())));
    }

    @Test
    void logsHistoryWithDataSource() throws SQLException {
        assertHistorySaved(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    private void assertHistorySaved(final UserHistoryDao userHistoryDao) throws SQLException {
        final var history = new UserHistory(new User(42L, "gugu", "new-password", "gugu@example.com"), "admin");

        userHistoryDao.log(history);

        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement();
             final var resultSet = statement.executeQuery("select * from user_history")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(42L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo("new-password");
            assertThat(resultSet.getString("email")).isEqualTo("gugu@example.com");
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.next()).isFalse();
        }
    }
}

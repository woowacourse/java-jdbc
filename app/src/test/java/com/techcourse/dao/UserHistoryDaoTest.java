package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
        }
    }

    @Test
    void dataSourceConstructorSavesHistory() throws SQLException {
        assertHistorySaved(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    @Test
    void jdbcTemplateConstructorSavesHistory() throws SQLException {
        assertHistorySaved(new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance())));
    }

    @Test
    void sqlFailureIsTranslated() {
        final var dao = new UserHistoryDao(DataSourceConfig.getInstance());
        final var history = new UserHistory(new User(1L, "gugu", "password", "gugu@example.com"), null);

        assertThatThrownBy(() -> dao.log(history))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    private void assertHistorySaved(final UserHistoryDao dao) throws SQLException {
        final var history = new UserHistory(new User(7L, "gugu", "password", "gugu@example.com"), "admin");

        dao.log(history);

        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(7L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo("password");
            assertThat(resultSet.getString("email")).isEqualTo("gugu@example.com");
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(resultSet.next()).isFalse();
        }
    }
}

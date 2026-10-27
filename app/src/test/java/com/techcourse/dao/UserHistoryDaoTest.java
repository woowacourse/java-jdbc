package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private final DataSource dataSource = DataSourceConfig.getInstance();

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(dataSource);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
        }
    }

    @Test
    void logsAllHistoryFields() throws SQLException {
        final var dao = new UserHistoryDao(dataSource);
        final var history = new UserHistory(
                new User(7L, "gugu", "changed-password", "gugu@woowahan.com"), "editor");

        dao.log(history);

        assertHistory(history);
    }

    @Test
    void acceptsJdbcTemplate() throws SQLException {
        final var dao = new UserHistoryDao(new JdbcTemplate(dataSource));
        final var history = new UserHistory(
                new User(7L, "gugu", "changed-password", "gugu@woowahan.com"), "editor");

        dao.log(history);

        assertHistory(history);
    }

    private void assertHistory(final UserHistory history) throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("id")).isEqualTo(1L);
            assertThat(resultSet.getLong("user_id")).isEqualTo(history.getUserId());
            assertThat(resultSet.getString("account")).isEqualTo(history.getAccount());
            assertThat(resultSet.getString("password")).isEqualTo(history.getPassword());
            assertThat(resultSet.getString("email")).isEqualTo(history.getEmail());
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(resultSet.getString("created_by")).isEqualTo(history.getCreateBy());
            assertThat(resultSet.next()).isFalse();
        }
    }
}

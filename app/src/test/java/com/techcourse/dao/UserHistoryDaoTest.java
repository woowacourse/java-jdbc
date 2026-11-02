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

    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
        }
    }

    @Test
    void logWithDataSource() throws SQLException {
        assertHistorySaved(new UserHistoryDao(dataSource));
    }

    @Test
    void logWithJdbcTemplate() throws SQLException {
        assertHistorySaved(new UserHistoryDao(new JdbcTemplate(dataSource)));
    }

    private void assertHistorySaved(final UserHistoryDao userHistoryDao) throws SQLException {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        final var history = new UserHistory(user, "admin");

        userHistoryDao.log(history);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select * from user_history")) {
            assertThat(resultSet.next()).isTrue();
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

package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    @Test
    void log() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User(1L, "gugu", "newPassword", "gugu@example.com");

        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");

            new UserHistoryDao(dataSource).log(new UserHistory(user, "admin"));

            try (final var result = statement.executeQuery("select * from user_history")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getLong("user_id")).isEqualTo(user.getId());
                assertThat(result.getString("account")).isEqualTo(user.getAccount());
                assertThat(result.getString("password")).isEqualTo(user.getPassword());
                assertThat(result.getString("email")).isEqualTo(user.getEmail());
                assertThat(result.getTimestamp("created_at")).isNotNull();
                assertThat(result.getString("created_by")).isEqualTo("admin");
                assertThat(result.next()).isFalse();
            }
        }
    }
}

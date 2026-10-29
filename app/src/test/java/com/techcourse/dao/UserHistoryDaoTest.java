package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @BeforeEach
    void setup() throws SQLException {
        assertThat(((org.h2.jdbcx.JdbcDataSource) DataSourceConfig.getInstance()).getURL())
                .as("초기화는 테스트 메모리 DB에서만 허용합니다. jdbc.url 설정을 확인하세요.")
                .startsWith("jdbc:h2:mem:");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
        }
    }

    @Test
    void savesHistoryUsingDataSource() throws SQLException {
        assertSavesHistory(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    @Test
    void savesHistoryUsingJdbcTemplate() throws SQLException {
        assertSavesHistory(new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance())));
    }

    @Test
    void savesHistoryUsingGivenConnection() throws SQLException {
        final var dao = new UserHistoryDao(DataSourceConfig.getInstance());
        final var history = new UserHistory(null, 7L, "gugu", "changed-password", "gugu@example.com", "admin");

        try (var connection = DataSourceConfig.getInstance().getConnection()) {
            dao.log(connection, history);
            assertThat(connection.isClosed()).isFalse();
        }

        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.prepareStatement("select user_id from user_history");
             var result = statement.executeQuery()) {
            assertThat(result.next()).isTrue();
            assertThat(result.getLong("user_id")).isEqualTo(7L);
        }
    }

    private void assertSavesHistory(final UserHistoryDao dao) throws SQLException {
        final var history = new UserHistory(null, 42L, "gugu", "changed-password",
                "gugu@example.com", "admin");

        dao.log(history);

        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.prepareStatement("select * from user_history");
             var result = statement.executeQuery()) {
            assertThat(result.next()).isTrue();
            assertThat(result.getLong("user_id")).isEqualTo(history.getUserId());
            assertThat(result.getString("account")).isEqualTo(history.getAccount());
            assertThat(result.getString("password")).isEqualTo(history.getPassword());
            assertThat(result.getString("email")).isEqualTo(history.getEmail());
            assertThat(result.getTimestamp("created_at").toLocalDateTime())
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(result.getString("created_by")).isEqualTo(history.getCreateBy());
            assertThat(result.next()).isFalse();
        }
    }
}

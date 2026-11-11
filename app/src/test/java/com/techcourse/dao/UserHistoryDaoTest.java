package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        new JdbcTemplate(dataSource).update("truncate table user_history restart identity");
    }

    @Test
    @DisplayName("사용자 변경 이력을 저장한다")
    void save_history() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        final var user = new User(7L, "gugu", "password", "hkkang@woowahan.com");
        final var history = new UserHistory(user, "admin");

        userHistoryDao.log(history);

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            assertHistorySaved(connection, history);
        }
    }

    @Test
    @DisplayName("전달받은 커넥션으로 이력을 저장하고 호출자가 커밋한다")
    void save_history_with_connection() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var history = new UserHistory(new User(7L, "gugu", "password", "hkkang@woowahan.com"), "admin");

        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            userHistoryDao.log(connection, history);

            assertHistorySaved(connection, history);
            final var historyCount = new JdbcTemplate(dataSource).queryForObject(
                    "select count(*) from user_history", row -> row.getLong(1));
            assertThat(historyCount).isZero();
            connection.commit();
        }

        try (final var connection = dataSource.getConnection()) {
            assertHistorySaved(connection, history);
        }
    }

    @Test
    @DisplayName("호출자가 롤백하면 전달받은 커넥션으로 저장한 이력도 취소된다")
    void rollback_history_with_connection() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var history = new UserHistory(new User(7L, "gugu", "password", "hkkang@woowahan.com"), "admin");

        try (final var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            userHistoryDao.log(connection, history);
            assertHistorySaved(connection, history);

            connection.rollback();

            final var historyCount = new JdbcTemplate(dataSource).queryForObject(connection,
                    "select count(*) from user_history", row -> row.getLong(1));
            assertThat(historyCount).isZero();
        }

        final var historyCount = new JdbcTemplate(dataSource).queryForObject(
                "select count(*) from user_history", row -> row.getLong(1));
        assertThat(historyCount).isZero();
    }

    private void assertHistorySaved(final Connection connection, final UserHistory history) throws SQLException {
        try (final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("id")).isEqualTo(1L);
            assertThat(resultSet.getLong("user_id")).isEqualTo(7L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo("password");
            assertThat(resultSet.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(resultSet.next()).isFalse();
        }
    }
}

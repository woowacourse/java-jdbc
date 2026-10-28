package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID());
    }

    @Test
    void 사용자_이력을_모든_필드와_함께_저장한다() throws SQLException {
        try (final var connection = dataSource.getConnection()) {
            DatabasePopulatorUtils.execute(dataSource);
            final var history = new UserHistory(new User(42L, "account", "password", "user@example.com"), "admin");

            new UserHistoryDao(dataSource).log(history);

            try (final var statement = connection.createStatement();
                 final var resultSet = statement.executeQuery("select * from user_history")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong("user_id")).isEqualTo(42L);
                assertThat(resultSet.getString("account")).isEqualTo("account");
                assertThat(resultSet.getString("password")).isEqualTo("password");
                assertThat(resultSet.getString("email")).isEqualTo("user@example.com");
                assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                        .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
                assertThat(resultSet.getString("created_by")).isEqualTo("admin");
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    void JdbcTemplate을_주입해도_사용자_이력을_저장한다() throws SQLException {
        try (final var connection = dataSource.getConnection()) {
            DatabasePopulatorUtils.execute(dataSource);
            final var history = new UserHistory(new User(42L, "account", "password", "user@example.com"), "admin");

            new UserHistoryDao(new JdbcTemplate(dataSource)).log(history);

            try (final var statement = connection.createStatement();
                 final var resultSet = statement.executeQuery("select count(*) from user_history")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void 사용자_이력_저장에_실패하면_예외를_전달한다() {
        final var history = new UserHistory(new User(42L, "account", "password", "user@example.com"), "admin");

        assertThatThrownBy(() -> new UserHistoryDao(dataSource).log(history))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }
}

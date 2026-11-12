package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private final DataSource dataSource = DataSourceConfig.getInstance();

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        DatabasePopulatorUtils.execute(dataSource);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
            statement.execute("truncate table users restart identity");
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void commitsPasswordChangeAndHistoryTogether() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));
        final var newPassword = "newPassword";
        final var createdBy = "editor";
        final var startedAt = LocalDateTime.now();

        userService.changePassword(1L, newPassword, createdBy);

        final var finishedAt = LocalDateTime.now();
        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", newPassword, "hkkang@woowahan.com"));
        final var histories = jdbcTemplate.query("select * from user_history", resultSet ->
                new HistorySnapshot(
                        resultSet.getLong("id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("account"),
                        resultSet.getString("password"),
                        resultSet.getString("email"),
                        resultSet.getObject("created_at", LocalDateTime.class),
                        resultSet.getString("created_by")
                ));
        assertThat(histories).singleElement().satisfies(history -> {
            assertThat(history.id()).isEqualTo(1L);
            assertThat(history.userId()).isEqualTo(1L);
            assertThat(history.account()).isEqualTo("gugu");
            assertThat(history.password()).isEqualTo(newPassword);
            assertThat(history.email()).isEqualTo("hkkang@woowahan.com");
            assertThat(history.createdBy()).isEqualTo(createdBy);
            assertThat(history.createdAt()).isBetween(startedAt.minusNanos(1_000), finishedAt.plusNanos(1_000));
        });
    }

    @Test
    void rollsBackPasswordWhenHistoryFailsBeforeInsert() {
        final var userService = new UserService(dataSource, userDao, new MockUserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "editor"))
                .isInstanceOf(DataAccessException.class);

        assertPasswordAndHistoryRolledBack();
    }

    @Test
    void rollsBackBothPasswordAndHistoryWhenFailureOccursAfterInsert() {
        final var failure = new DataAccessException("Failure after inserting history");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory userHistory) {
                super.log(connection, userHistory);
                throw failure;
            }
        };
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "editor"))
                .isSameAs(failure);

        assertPasswordAndHistoryRolledBack();
    }

    @Test
    void rollsBackPasswordWhenHistoryViolatesDatabaseConstraint() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(jdbcTemplate));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertPasswordAndHistoryRolledBack();
    }

    private void assertPasswordAndHistoryRolledBack() {
        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history", resultSet -> resultSet.getLong(1));
        assertThat(historyCount).isZero();
    }

    private record HistorySnapshot(long id, long userId, String account, String password, String email,
                                   LocalDateTime createdAt, String createdBy) {
    }
}

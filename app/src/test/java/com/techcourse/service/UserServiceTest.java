package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private Connection databaseConnection;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID());
        dataSource = jdbcDataSource;
        databaseConnection = dataSource.getConnection();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        databaseConnection.close();
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);
        final var before = LocalDateTime.now().minusSeconds(1);
        final var newPassword = "qqqqq";
        final var createBy = "gugu";

        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);

        final var histories = jdbcTemplate.query(
                "select user_id, password, created_by, created_at from user_history",
                row -> new History(row.getLong("user_id"), row.getString("password"),
                        row.getString("created_by"), row.getTimestamp("created_at").toLocalDateTime()));
        assertThat(histories).hasSize(1);
        final var history = histories.getFirst();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.createdBy()).isEqualTo(createBy);
        assertThat(history.createdAt()).isBetween(before, LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void testTransactionRollback() {
        // 이력을 저장한 뒤 예외를 발생시켜 두 작업이 모두 롤백되는지 확인한다.
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertPasswordAndHistoryUnchanged(userService);
    }

    @Test
    void historyConstraintViolationRollsBackPassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(failure).hasCauseInstanceOf(SQLException.class);
        assertPasswordAndHistoryUnchanged(userService);
    }

    private void assertPasswordAndHistoryUnchanged(final UserService userService) {
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final Long historyCount = jdbcTemplate.queryForObject("select count(*) from user_history",
                row -> row.getLong(1));
        assertThat(historyCount).isZero();
    }

    private record History(long userId, String password, String createdBy, LocalDateTime createdAt) {
    }
}

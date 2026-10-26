package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private javax.sql.DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "support-agent";
        final var startedAt = LocalDateTime.now().minusSeconds(1);
        userService.changePassword(1L, newPassword, createBy);
        final var finishedAt = LocalDateTime.now().plusSeconds(1);

        final var actual = userService.findById(1L);
        final var history = jdbcTemplate.queryForObject(
                "select user_id, password, created_by, created_at from user_history",
                resultSet -> new HistoryRecord(
                        resultSet.getLong("user_id"),
                        resultSet.getString("password"),
                        resultSet.getString("created_by"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()));

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(history).isNotNull();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.createdBy()).isEqualTo(createBy);
        assertThat(history.createdAt()).isBetween(startedAt, finishedAt);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testTransactionRollsBackWhenUserUpdateAffectsNoRows() {
        final var userDaoWithNoUpdatedRows = new UserDao(jdbcTemplate) {
            @Override
            public int update(final Connection connection, final User user) {
                jdbcTemplate.update(connection, "delete from users where id = ?", user.getId());
                return 0;
            }
        };
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDaoWithNoUpdatedRows, userHistoryDao);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "support-agent"));

        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", resultSet -> resultSet.getInt(1));
    }

    private record HistoryRecord(long userId, String password, String createdBy, LocalDateTime createdAt) {}
}

package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("truncate table users restart identity");
        jdbcTemplate.update("truncate table user_history restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var startedAt = LocalDateTime.now();
        userService.changePassword(1L, newPassword, createBy);
        final var completedAt = LocalDateTime.now();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final var historyUserId = jdbcTemplate.queryForObject("select * from user_history where user_id = ?", rs -> {
            assertThat(rs.getString("password")).isEqualTo(newPassword);
            assertThat(rs.getString("created_by")).isEqualTo(createBy);
            assertThat(rs.getTimestamp("created_at").toLocalDateTime()).isBetween(startedAt, completedAt);
            return rs.getLong("user_id");
        }, 1L);
        assertThat(historyUserId).isEqualTo(1L);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isZero();
    }

    @Test
    void testTransactionRollbackAfterHistoryInsert() {
        final var failure = new DataAccessException("history failed");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory userHistory) {
                super.log(connection, userHistory);
                throw failure;
            }
        };
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final var historyCount = jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getLong(1));
        assertThat(historyCount).isZero();
    }
}

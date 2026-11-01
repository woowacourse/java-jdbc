package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import java.sql.Connection;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(DataSourceConfig.getInstance(), userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final var historyIds = jdbcTemplate.query("select * from user_history where user_id = ?", rs -> {
            assertThat(rs.getString("password")).isEqualTo(newPassword);
            assertThat(rs.getString("created_by")).isEqualTo(createBy);
            assertThat(rs.getTimestamp("created_at")).isNotNull();
            return rs.getLong("id");
        }, actual.getId());
        assertThat(historyIds).hasSize(1);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(DataSourceConfig.getInstance(), userDao, userHistoryDao);

        final var originalPassword = userService.findById(1L).getPassword();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        final var historyIds = jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"));
        assertThat(historyIds).isEmpty();
        assertThat(actual.getPassword()).isEqualTo(originalPassword);
    }

    @Test
    void rollsBackPasswordAndHistoryWhenFailureOccursAfterHistoryInsert() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw new DataAccessException("failure after history insert");
            }
        };
        final var userService = new UserService(DataSourceConfig.getInstance(), userDao, userHistoryDao);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "admin"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong("id"))).isEmpty();
    }

}

package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private User user;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void refresh() {
        jdbcTemplate.update("DELETE FROM user_history");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void testChangePassword() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        final UserService userService = new UserService(userDao, userHistoryDao);

        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        userService.changePassword(user.getId(), newPassword, createBy);

        final User actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final MockUserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao);

        final String newPassword = "newPassword";
        final String createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(user.getId(), newPassword, createBy));

        final User actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

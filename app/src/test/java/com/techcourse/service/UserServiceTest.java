package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserServiceTest {
    private static final Long USER_ID = 1L;
    private static final String NEW_PASSWORD = "qqqqq";
    private static final String CREATED_BY = "gugu";

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE users RESTART IDENTITY");
    }

    @Test
    void testChangePassword() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao);

        userService.changePassword(USER_ID, NEW_PASSWORD, CREATED_BY);

        final User actual = userService.findById(USER_ID);

        assertThat(actual.getPassword()).isEqualTo(NEW_PASSWORD);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao);

        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATED_BY));

        final User actual = userService.findById(USER_ID);

        assertThat(actual.getPassword()).isNotEqualTo(NEW_PASSWORD);
    }
}

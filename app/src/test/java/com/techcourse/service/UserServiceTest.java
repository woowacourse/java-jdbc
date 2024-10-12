package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.exception.TransactionFailedException;
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

    @DisplayName("패스워드를 갱신할 수 있다.")
    @Test
    void testChangePassword() {
        // given
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao);

        // when
        userService.changePassword(USER_ID, NEW_PASSWORD, CREATED_BY);

        // then
        final User actual = userService.findById(USER_ID);

        assertThat(actual.getPassword()).isEqualTo(NEW_PASSWORD);
    }

    @DisplayName("MockUserHistoryDao에서 예외가 발생하면, 롤백되면서 패스워드 갱신을 하지 않는다.")
    @Test
    void testTransactionRollback() {
        // given
        final UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao);

        // when
        assertThrows(TransactionFailedException.class,
                () -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATED_BY));

        // then
        final User actual = userService.findById(USER_ID);

        assertThat(actual.getPassword()).isNotEqualTo(NEW_PASSWORD);
    }
}

package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance()); // DB 초기화
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao);

        final var user = userDao.findByAccount("gugu");
        final var newPassword = "qqqqq";
        final var createdBy = "gugu";
        userService.changePassword(user.get().getId(), newPassword, createdBy);

        final var actual = userService.findById(user.get().getId());
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao);

        final var user = userDao.findByAccount("gugu");
        final var newPassword = "newPassword";
        final var createdBy = "gugu";

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(user.get().getId(), newPassword, createdBy));

        final var actual = userService.findById(user.get().getId());
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

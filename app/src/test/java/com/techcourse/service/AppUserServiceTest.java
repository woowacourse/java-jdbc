package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private User user;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao.deleteAll();

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        this.user = userDao.findByAccount("gugu");
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(user.getId(), newPassword, createBy);

        final var actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}

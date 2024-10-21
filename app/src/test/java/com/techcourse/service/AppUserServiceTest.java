package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");

        UserDao userDao = new UserDao(jdbcTemplate);
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        this.appUserService = new AppUserService(userDao, userHistoryDao);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findById() {
        User user = appUserService.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void insert() {
        final var user = new User("gugu2", "password", "hkkang@woowahan.com");
        appUserService.insert(user);

        final var findUser = appUserService.findById(2L);
        assertThat(findUser.getAccount()).isEqualTo("gugu2");
    }

    @Test
    void changePassword() {
        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        appUserService.changePassword(1L, newPassword, createBy);

        final var actual = appUserService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
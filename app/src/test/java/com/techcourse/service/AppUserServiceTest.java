package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

    private AppUserService appUserService;
    private User savedUser;

    @BeforeEach
    void setUp() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final UserDao userDao = new UserDao(jdbcTemplate);
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        this.appUserService = new AppUserService(userDao, userHistoryDao);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        this.savedUser = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(savedUser);
    }

    @Test
    void findById() {
        final User actual = appUserService.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(savedUser);
    }

    @Test
    void insert() {
        final User user = new User("forky", "password", "forky@woowahan.com");

        assertThatNoException()
                .isThrownBy(() -> appUserService.insert(user));
    }

    @Test
    void testChangePassword() {
        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        appUserService.changePassword(1L, newPassword, createBy);

        final User actual = appUserService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}

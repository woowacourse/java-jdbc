package com.techcourse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppUserServiceTest {

    private UserService appUserService;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        this.appUserService = new AppUserService(userDao, userHistoryDao);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        jdbcTemplate.update("DROP TABLE IF EXISTS user_history");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @DisplayName("새로운 사용자를 저장한다.")
    @Test
    void insert() {
        // given
        User user = new User("gugu", "password", "hkkang@woowahan.com");

        // when
        appUserService.insert(user);

        // then
        User actual = appUserService.getById(1L);

        assertThat(actual.getAccount()).isEqualTo(user.getAccount());
    }

    @DisplayName("사용자의 비밀번호를 변경한다.")
    @Test
    void testChangePassword() {
        // given
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        String newPassword = "qqqqq";
        String createBy = "gugu";
        appUserService.changePassword(1L, newPassword, createBy);

        // then
        User actual = appUserService.getById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("사용자를 찾을 수 없으면 예외가 발생한다.")
    @Test
    void cannotChangePassword() {
        // when & then
        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThatThrownBy(() -> appUserService.changePassword(2L, newPassword, createBy))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

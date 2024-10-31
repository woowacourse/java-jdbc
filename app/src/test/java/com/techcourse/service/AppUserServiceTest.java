package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        UserDao userDao = new UserDao(jdbcTemplate);
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        this.appUserService = new AppUserService(userDao, userHistoryDao);
    }

    @DisplayName("id로 User를 조회할 수 있다.")
    @Test
    void findById() {
        // given
        appUserService.insert(new User("ever", "password", "ever@woowahan.com"));

        // when
        User user = appUserService.findById(1L);

        // then
        assertThat(user.getAccount()).isEqualTo("ever");
    }

    @DisplayName("User를 추가할 수 있다.")
    @Test
    void insert() {
        // given
        User newUser = new User("ever2", "password", "eve2r@woowahan.com");

        // when
        appUserService.insert(newUser);

        // then
        assertThat(appUserService.findById(1L).getAccount()).isEqualTo("ever2");
    }

    @DisplayName("User의 비밀번호를 변경할 수 있다.")
    @Test
    void changePassword() {
        // given
        appUserService.insert(new User("ever", "password", "ever@woowahan.com"));

        // when
        appUserService.changePassword(1L, "newPassword", "ever");

        // then
        assertThat(appUserService.findById(1L).getPassword()).isEqualTo("newPassword");
    }

    @DisplayName("User의 비밀번호를 변경하면 로그가 기록된다.")
    @Test
    void should_log_when_changePassword() {
        // given
        appUserService.insert(new User("ever", "password", "ever@woowahan.com"));

        // when
        appUserService.changePassword(1L, "newPassword", "ever");

        // then
        assertThat(appUserService.findById(1L).getPassword()).isEqualTo("newPassword");
    }
}

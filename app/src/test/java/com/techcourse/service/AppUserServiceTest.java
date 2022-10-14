package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    private static final String USER_ACCOUNT = "gugu";
    private static final User USER = new User(USER_ACCOUNT, "password", "hkkang@woowahan.com");

    private long savedUserId;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);

        DatabasePopulatorUtils.init(DataSourceConfig.getInstance());

        userDao.insert(USER);
        savedUserId = userDao.findByAccount(USER.getAccount()).getId();
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(DataSourceConfig.getInstance());
    }

    @DisplayName("아이디를 이용하여 사용자 정보 조회")
    @Test
    void findById() {
        final AppUserService userService = new AppUserService(userDao, userHistoryDao);

        final User user = userService.findById(savedUserId);

        assertThat(user).usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(USER);
    }

    @DisplayName("사용자 저장")
    @Test
    void insert() {
        final AppUserService userService = new AppUserService(userDao, userHistoryDao);

        final User user = new User("newAccount", "newPassword", "newEmail@email.com");
        userService.insert(user);

        final User savedUser = userDao.findByAccount(user.getAccount());
        assertThat(user).usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(savedUser);
    }

    @DisplayName("사용자 비밀번호 변경")
    @Test
    void changePassword() {
        final AppUserService userService = new AppUserService(userDao, userHistoryDao);

        userService.changePassword(savedUserId, "newPassword", "gugu");

        final User updatedUser = userDao.findById(savedUserId);
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword");
    }
}

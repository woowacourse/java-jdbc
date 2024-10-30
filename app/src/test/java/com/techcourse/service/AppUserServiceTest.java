package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserServiceTest {

    private static final String ACCOUNT_GUGU = "gugu";
    private static final String INITIAL_PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private AppUserService userService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        userService = new AppUserService(userDao, userHistoryDao);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User(ACCOUNT_GUGU, INITIAL_PASSWORD, "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testSave() {
        //given
        User user = new User("Ash", "password", "ash@techcourse.com");

        //when
        userService.save(user);
        long id = userDao.findAll()
                .getLast()
                .getId();

        User foundUser = userDao.findById(id).get();

        //then
        assertThat(foundUser.getAccount()).isEqualTo("Ash");
    }

    @Test
    void testChangePassword() {
        //when
        userService.changePassword(1L, NEW_PASSWORD, ACCOUNT_GUGU);

        User actual = userService.findById(1L).get();

        //then
        assertThat(actual.getPassword()).isEqualTo(NEW_PASSWORD);
    }
}

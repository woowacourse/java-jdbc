package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);

        this.userDao = new UserDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        this.userService = new TxUserService(jdbcTemplate, appUserService);

        DatabasePopulatorUtils.execute(dataSource);
    }

    @Test
    void testChangePassword() {
        // given
        String account = "gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        String newPassword = "qqqqq";

        // when
        userService.changePassword(1L, newPassword, account);

        // then
        User actual = userService.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // given
        String account = "gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        String newPassword = "qqqqq";

        MockUserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        this.userService = new TxUserService(jdbcTemplate, appUserService);

        // when
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, account));

        // then
        User actual = userService.findById(1L);
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

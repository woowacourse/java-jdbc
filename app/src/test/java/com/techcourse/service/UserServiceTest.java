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
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(dataSource, userDao, userHistoryDao);
        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(dataSource, userDao, userHistoryDao);
        String newPassword = "newPassword";
        String createBy = "gugu";

        userService.changePassword(1L, newPassword, createBy);
        String actualPassword = userService.findById(1L).getPassword();

        assertThat(actualPassword).isNotEqualTo(newPassword);
    }
}

package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // given
        final var mockUserHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, mockUserHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";

        // when
        try {
            userService.changePassword(1L, newPassword, createBy);
        } catch (final Exception e) {
            // do nothing
        }

        // then
        final var actual = userService.findById(1L);
        assertAll(
                () -> assertThrows(DataAccessException.class,
                        () -> userService.changePassword(1L, newPassword, createBy)),
                () -> assertThat(actual.getPassword()).isNotEqualTo(newPassword)
        );
    }
}

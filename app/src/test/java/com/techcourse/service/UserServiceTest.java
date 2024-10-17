package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

    @AfterEach
    void tearDown() {
        String truncateUsers = "TRUNCATE TABLE users RESTART IDENTITY";
        String truncateUserHistory = "TRUNCATE TABLE user_history RESTART IDENTITY";
        jdbcTemplate.update(truncateUsers);
        jdbcTemplate.update(truncateUserHistory);
    }

    @DisplayName("사용자의 password를 변경한다.")
    @Test
    void testChangePassword() {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";

        // when
        userService.changePassword(1L, newPassword, createBy);

        // then
        assertThat(userService.findById(1L).getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("password를 변경하다가 예외가 발생하면, 트랜잭션이 롤백되어 변경이 취소된다.")
    @Test
    void testTransactionRollback() {
        // given
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "newPassword";
        final var createdBy = "gugu";

        // when
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createdBy));

        // then
        assertThat(userService.findById(1L).getPassword()).isNotEqualTo(newPassword);
    }
}

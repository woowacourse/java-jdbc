package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
    }

    @AfterEach
    void teardown() {
        final var sql = "TRUNCATE TABLE users";
        jdbcTemplate.update(sql);
    }

    @Test
    void testChangePassword() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        final var id = userDao.insert(user);

        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, DataSourceConfig.getInstance());

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(id, newPassword, createBy);

        final var actual = userService.findById(id);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        final var id = userDao.insert(user);

        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, DataSourceConfig.getInstance());

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(
                DataAccessException.class,
                () -> userService.changePassword(id, newPassword, createBy)
        );

        final var actual = userService.findById(id);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

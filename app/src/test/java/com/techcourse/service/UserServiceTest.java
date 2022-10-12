package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testChangePassword() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        UserService userService = new TxUserService(DataSourceConfig.getInstance(), appUserService);

        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        UserService userService = new TxUserService(DataSourceConfig.getInstance(), appUserService);

        String newPassword = "newPassword";
        String createBy = "gugu";

        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

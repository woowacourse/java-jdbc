package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

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
        var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        var transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
        var appUserService = new AppUserService(userDao, userHistoryDao);
        var userService = new TxUserService(transactionTemplate, appUserService);

        var newPassword = "qqqqq";
        var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        var transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
        // 애플리케이션 서비스
        var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        var userService = new TxUserService(transactionTemplate, appUserService);

        var newPassword = "newPassword";
        var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

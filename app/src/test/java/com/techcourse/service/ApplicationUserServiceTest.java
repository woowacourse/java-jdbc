package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationUserServiceTest {

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
    void testFindById() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new ApplicationUserService(userDao, userHistoryDao);
        final TransactionUserService transactionUserService = new TransactionUserService(userService);

        final var actual = transactionUserService.findById(1L);

        assertThat(actual).isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void testInsert() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new ApplicationUserService(userDao, userHistoryDao);
        final TransactionUserService transactionUserService = new TransactionUserService(userService);

        final User newUser = new User("tiki", "password", "tiki@woowahan.com");
        transactionUserService.insert(newUser);

        final User actual = transactionUserService.findById(2L);
        assertThat(actual).isEqualTo(new User(2L, "tiki", "password", "tiki@woowahan.com"));
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new ApplicationUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new ApplicationUserService(userDao, userHistoryDao);
        final TransactionUserService transactionUserService = new TransactionUserService(userService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> transactionUserService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

package com.techcourse.service;

import com.interface21.jdbc.transaction.TransactionManager;
import com.interface21.jdbc.transaction.TransactionProxy;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private TransactionManager transactionManager;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.transactionManager = new TransactionManager();
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance(), transactionManager);
        this.userDao = new UserDao(jdbcTemplate);

        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = TransactionProxy.createProxy(
                new UserServiceImpl(userDao, userHistoryDao),
                UserService.class,
                DataSourceConfig.getInstance(),
                transactionManager
        );

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
        UserServiceImpl userServiceImpl = new UserServiceImpl(userDao, userHistoryDao);
        UserService userService = TransactionProxy.createProxy(
                userServiceImpl,
                UserService.class,
                DataSourceConfig.getInstance(),
                transactionManager
        );

        String newPassword = "newPassword";
        String createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

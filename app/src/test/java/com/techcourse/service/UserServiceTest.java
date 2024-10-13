package com.techcourse.service;

import com.interface21.jdbc.exception.CannotGetJdbcConnectionException;
import com.interface21.transaction.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransactionManager transactionManager;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);
        this.transactionManager = new TransactionManager();
        this.connection = DataSourceConfig.getInstance().getConnection();

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, transactionManager);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(connection, 1L, newPassword, createBy);

        final var actual = userService.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, transactionManager);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.

        assertThrows(CannotGetJdbcConnectionException.class,
                () -> userService.changePassword(connection, 1L, newPassword, createBy));

        final var actual = userService.findById(1L).get();

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}

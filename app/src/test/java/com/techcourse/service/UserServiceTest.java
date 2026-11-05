package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        resetTables(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(countHistory()).isEqualTo(1);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
        assertThat(countHistory()).isZero();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void failedTransactionDoesNotPreventTheNextTransaction() {
        final UserService userService = new TxUserService(dataSource, new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        assertThrows(DataAccessException.class, () -> userService.changePassword(1L, "badPassword", null));
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        userService.changePassword(1L, "goodPassword", "gugu");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("goodPassword");
        assertThat(countHistory()).isEqualTo(1);
    }

    private int countHistory() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", resultSet -> resultSet.getInt(1));
    }

    private static void resetTables(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("truncate table user_history restart identity");
            statement.execute("truncate table users restart identity");
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to reset tables for transaction tests", exception);
        }
    }
}

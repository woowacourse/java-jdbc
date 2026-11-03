package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(
                DataSourceConfig.getInstance(), userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(historyPasswords()).containsExactly(newPassword);
        final var author = jdbcTemplate.queryForObject(
                "select created_by from user_history where user_id = ?",
                resultSet -> resultSet.getString("created_by"), 1L);
        assertThat(author).isEqualTo(createBy);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(
                DataSourceConfig.getInstance(), userDao, userHistoryDao);

        final var originalPassword = userService.findById(1L).getPassword();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
        assertThat(historyPasswords()).isEmpty();
    }

    @Test
    void testTransactionRollbackWhenHistoryInsertFails() {
        final var userService = new UserService(
                DataSourceConfig.getInstance(), userDao, new UserHistoryDao(jdbcTemplate));
        final var originalPassword = userService.findById(1L).getPassword();

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "new-password", null));

        assertThat(failure).hasCauseInstanceOf(SQLException.class);
        assertThat(userService.findById(1L).getPassword()).isEqualTo(originalPassword);
        assertThat(historyPasswords()).isEmpty();
    }

    @Test
    void testRollbackFailurePreservesOriginalException() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var transactionUserDao = mock(UserDao.class);
        final var transactionHistoryDao = mock(UserHistoryDao.class);
        final var originalException = new DataAccessException("History logging failed");
        final var rollbackException = new SQLException("Rollback failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(transactionUserDao.findById(connection, 1L))
                .thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        doThrow(originalException).when(transactionHistoryDao)
                .log(eq(connection), any(UserHistory.class));
        doThrow(rollbackException).when(connection).rollback();
        final var userService = new UserService(
                dataSource, transactionUserDao, transactionHistoryDao);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "new-password", "gugu"));

        assertThat(actual).isSameAs(originalException);
        assertThat(actual.getSuppressed()).containsExactly(rollbackException);
        verify(connection).close();
        verify(connection, never()).setAutoCommit(true);
    }

    private List<String> historyPasswords() {
        return jdbcTemplate.query("select password from user_history where user_id = ?",
                resultSet -> resultSet.getString("password"), 1L);
    }
}

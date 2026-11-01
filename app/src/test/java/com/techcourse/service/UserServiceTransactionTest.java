package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(connection, 42L))
                .thenReturn(new User(42L, "gugu", "password", "user@example.com"));
        userService = new UserService(dataSource, userDao, userHistoryDao);
    }

    @Test
    void 같은_연결로_조회와_변경과_이력_저장을_끝낸_뒤_커밋하고_닫는다() throws SQLException {
        userService.changePassword(42L, "newPassword", "admin");

        final var order = inOrder(dataSource, connection, userDao, userHistoryDao);
        order.verify(dataSource).getConnection();
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 42L);
        order.verify(userDao).update(eq(connection), any(User.class));
        order.verify(userHistoryDao).log(eq(connection), any());
        order.verify(connection).commit();
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 커밋이_실패하면_롤백하고_원인_예외를_보존한다() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(42L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void 롤백도_실패하면_원래_예외를_유지하고_롤백_실패를_추가한다() throws SQLException {
        final var failure = new DataAccessException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any());
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(42L, "newPassword", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void Error가_발생해도_롤백하고_연결을_닫은_뒤_그대로_전달한다() throws SQLException {
        final var failure = new AssertionError("history failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any());

        assertThatThrownBy(() -> userService.changePassword(42L, "newPassword", "admin"))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection).close();
        verify(connection, never()).commit();
    }
}

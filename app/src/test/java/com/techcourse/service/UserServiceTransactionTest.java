package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        user = new User(1L, "gugu", "password", "gugu@example.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
        userService = new UserService(dataSource, userDao, userHistoryDao);
    }

    @Test
    void commitsAfterBothWritesAndRestoresConnection() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(userHistoryDao).log(eq(connection), any());
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(dataSource).getConnection();
        verify(connection, never()).rollback();
    }

    @Test
    void runtimeFailureRollsBackAndPreservesOriginalException() throws SQLException {
        final var failure = new IllegalStateException("history failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any());

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void commitFailureRollsBackAndPreservesSqlException() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollbackAndCloseFailuresAreSuppressedWithoutEnablingAutoCommit() throws SQLException {
        final var failure = new IllegalStateException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any());
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure)
                .hasSuppressedException(rollbackFailure)
                .hasSuppressedException(closeFailure);

        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    void connectionAcquisitionFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(userDao, never()).update(any(Connection.class), any(User.class));
    }

    @Test
    void closeFailureAfterCommitPreservesCause() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }
}

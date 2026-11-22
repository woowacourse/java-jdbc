package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
    private final User user = new User(1L, "gugu", "password", "gugu@example.com");
    private final UserService userService = new UserService(userDao, userHistoryDao, dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
    }

    @Test
    void commitsBothWritesBeforeRestoringAndClosingConnection() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(userHistoryDao).log(eq(connection), any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackWhenCommitFails() throws SQLException {
        final var failure = new SQLException("Commit failed");
        doThrow(failure).when(connection).commit();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(failure);
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void preservesBusinessFailureWhenRollbackFails() throws SQLException {
        final var failure = new DataAccessException("History failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        assertThat(actual.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void preservesBusinessFailureWhenRestoringAutoCommitFails() throws SQLException {
        final var failure = new DataAccessException("History failed");
        final var restoreFailure = new SQLException("Restore failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        assertThat(actual.getSuppressed()).containsExactly(restoreFailure);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void closesConnectionWhenStartingTransactionFails() throws SQLException {
        final var failure = new SQLException("Start failed");
        doThrow(failure).when(connection).setAutoCommit(false);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(failure);
        verify(connection).close();
        verifyNoInteractions(userDao, userHistoryDao);
    }
}

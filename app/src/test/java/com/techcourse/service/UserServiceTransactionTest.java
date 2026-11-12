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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
    private User user;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        user = new User(1L, "eian", "old-password", "eian@example.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
        userService = new UserService(dataSource, userDao, userHistoryDao);
    }

    @Test
    void sharesOneConnectionAndCommitsBeforeRestoringAndClosingIt() throws SQLException {
        userService.changePassword(1L, "new-password", "admin");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(userHistoryDao).log(eq(connection), any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(dataSource).getConnection();
        verify(connection, never()).rollback();
        assertThat(user.getPassword()).isEqualTo("new-password");
    }

    @Test
    void rollsBackAndPreservesRuntimeFailure() throws SQLException {
        final var failure = new IllegalStateException("history could not be saved");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRollbackThenRestoreAndClose();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackAndPreservesError() throws SQLException {
        final var failure = new AssertionError("history callback failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRollbackThenRestoreAndClose();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackWhenCommitFailsAndPreservesSQLExceptionCause() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void suppressesRollbackFailureWithoutEnablingAutoCommit() throws SQLException {
        final var failure = new IllegalStateException("history could not be saved");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).close();
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    void suppressesRestoreFailureAfterRollbackWithoutLosingOriginalFailure() throws SQLException {
        final var failure = new IllegalStateException("history could not be saved");
        final var restoreFailure = new SQLException("auto commit restore failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verifyRollbackThenRestoreAndClose();
        verify(connection, never()).commit();
    }

    @Test
    void reportsRestoreFailureAfterCommitWithoutAttemptingRollback() throws SQLException {
        final var failure = new SQLException("auto commit restore failed");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void preservesOriginallyDisabledAutoCommit() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        userService.changePassword(1L, "new-password", "admin");

        verify(connection).getAutoCommit();
        verify(connection, atLeastOnce()).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    private void verifyRollbackThenRestoreAndClose() throws SQLException {
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }
}

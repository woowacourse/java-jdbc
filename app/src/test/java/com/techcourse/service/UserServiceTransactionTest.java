package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService appUserService = mock(UserService.class);
    private final UserService userService = new TxUserService(appUserService, dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @AfterEach
    void leavesNoTransactionConnectionBound() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void commitsAfterBusinessLogicAndClosesConnection() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(appUserService, connection);
        order.verify(appUserService).changePassword(1L, "newPassword", "gugu");
        order.verify(connection).commit();

        verify(connection).setAutoCommit(false);
        verify(connection).setAutoCommit(true);
        verify(connection).close();
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
        order.verify(connection).close();
        verify(connection).setAutoCommit(true);
    }

    @Test
    void preservesBusinessFailureWhenRollbackFails() throws SQLException {
        final var failure = new DataAccessException("History failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(failure).when(appUserService).changePassword(1L, "newPassword", "gugu");
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
        doThrow(failure).when(appUserService).changePassword(1L, "newPassword", "gugu");
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
        verifyNoInteractions(appUserService);
    }

    @Test
    void preservesBusinessFailureWhenClosingConnectionFails() throws SQLException {
        final var failure = new IllegalStateException("Business logic failed");
        final var closeFailure = new SQLException("Close failed");
        doThrow(failure).when(appUserService).changePassword(1L, "newPassword", "gugu");
        doThrow(closeFailure).when(connection).close();

        final var actual = assertThrows(IllegalStateException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        assertThat(actual.getSuppressed()).singleElement().satisfies(suppressed ->
                assertThat(suppressed).isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(closeFailure));
        verify(connection).rollback();
    }

    @Test
    void rollsBackAndCleansUpOnError() throws SQLException {
        final var failure = new AssertionError("Business logic failed");
        doThrow(failure).when(appUserService).changePassword(1L, "newPassword", "gugu");

        final var actual = assertThrows(AssertionError.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        verify(connection).rollback();
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void delegatesFindAndSaveWithoutStartingTransaction() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(appUserService.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
        userService.save(user);
        userService.insert(user);

        verify(appUserService, times(2)).save(user);
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    void rejectsNestedTransactionWithoutClosingOuterConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThrows(IllegalStateException.class,
                    () -> userService.changePassword(1L, "newPassword", "gugu"));

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            verifyNoInteractions(appUserService, dataSource, connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

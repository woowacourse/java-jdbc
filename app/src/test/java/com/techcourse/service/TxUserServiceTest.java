package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "new-password", "admin");
        userService = new TxUserService(dataSource, delegate);
    }

    @AfterEach
    void verifiesThreadResourceWasReleased() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    @Test
    void bindsConnectionForDelegateAndCommitsBeforeRestoringAndClosingIt() throws SQLException {
        userService.changePassword(1L, "new-password", "admin");

        final var order = inOrder(connection, delegate);
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(1L, "new-password", "admin");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(dataSource).getConnection();
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackAndPreservesRuntimeFailure() throws SQLException {
        final var failure = new IllegalStateException("history could not be saved");
        failBusinessWith(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRollbackThenRestoreAndClose();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackAndPreservesError() throws SQLException {
        final var failure = new AssertionError("history callback failed");
        failBusinessWith(failure);

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
        failBusinessWith(failure);
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
        failBusinessWith(failure);
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

    @Test
    void suppressesReleaseFailureWithoutLosingBusinessFailure() throws SQLException {
        final var failure = new IllegalStateException("history could not be saved");
        final var closeFailure = new SQLException("connection close failed");
        failBusinessWith(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).hasSize(1);
        assertThat(failure.getSuppressed()[0])
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
        verifyRollbackThenRestoreAndClose();
    }

    @Test
    void reportsReleaseFailureAfterCommitAndUnbindsConnection() throws SQLException {
        final var failure = new SQLException("connection close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);

        verify(connection).commit();
        verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void closesConnectionWhenReadingAutoCommitFailsWithoutCallingDelegate() throws SQLException {
        final var failure = new SQLException("auto commit state unavailable");
        when(connection.getAutoCommit()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
        verifyNoInteractions(delegate);
        verify(connection, never()).commit();
    }

    @Test
    void closesConnectionWhenStartingTransactionFailsWithoutCallingDelegate() throws SQLException {
        final var failure = new SQLException("cannot disable auto commit");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
        verifyNoInteractions(delegate);
        verify(connection, never()).commit();
    }

    @Test
    void rejectsExistingBindingWithoutModifyingOrClosingItsConnection() {
        final var existingConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, existingConnection);
        try {
            assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(existingConnection);
            verifyNoInteractions(dataSource, connection, existingConnection, delegate);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void failBusinessWith(final Throwable failure) {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            throw failure;
        }).when(delegate).changePassword(1L, "new-password", "admin");
    }

    private void verifyRollbackThenRestoreAndClose() throws SQLException {
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }
}

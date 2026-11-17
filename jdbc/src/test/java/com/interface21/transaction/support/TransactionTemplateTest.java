package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Runnable action = mock(Runnable.class);
    private final TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void runsActionInsideTransactionAndCommitsBeforeRestoringAndClosing() throws SQLException {
        execute();

        final var order = inOrder(connection, action);
        order.verify(connection).setAutoCommit(false);
        order.verify(action).run();
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource).getConnection();
    }

    @Test
    void returnsResultAfterCommit() throws SQLException {
        assertThat(transactionTemplate.execute(() -> "result")).isEqualTo("result");

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void bindsConnectionOnlyWhileActionRuns() {
        final List<Connection> boundDuringAction = new ArrayList<>();
        doAnswer(invocation -> boundDuringAction.add(TransactionSynchronizationManager.getResource(dataSource)))
                .when(action).run();

        execute();

        assertThat(boundDuringAction).containsExactly(connection);
    }

    @Test
    void rejectsNestedTransactionWithoutRunningActionOrTouchingOuterConnection() throws SQLException {
        final Connection outer = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, outer);
        try {
            assertThatThrownBy(this::execute).isInstanceOf(IllegalStateException.class);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(outer);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        verifyNoInteractions(action, outer);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void outerTransactionCannotCommitSwallowedNestedFailure() throws SQLException {
        final Runnable inner = mock(Runnable.class);

        transactionTemplate.executeWithoutResult(() ->
                assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(inner))
                        .isInstanceOf(IllegalStateException.class));

        verifyNoInteractions(inner);
        verify(dataSource).getConnection();
        verify(connection).commit();
    }

    @Test
    void rollsBackRuntimeFailureBeforeRestoringAndClosing() throws SQLException {
        final var failure = failAction();

        assertThatThrownBy(this::execute).isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackErrorAndRethrowsIt() throws SQLException {
        final var failure = new AssertionError("unexpected");
        doThrow(failure).when(action).run();

        assertThatThrownBy(this::execute).isSameAs(failure);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void commitFailureAttemptsRollbackAndPreservesCause() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(this::execute)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void rollbackFailureSkipsAutoCommitRestorationAndPreservesFailures() throws SQLException {
        final var failure = failAction();
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(this::execute).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure, closeFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void runtimeCloseFailureDoesNotReplaceBusinessFailure() throws SQLException {
        final var failure = failAction();
        final var closeFailure = new IllegalStateException("driver bug on close");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(this::execute).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    void restorationFailureAfterRollbackDoesNotReplaceBusinessFailure() throws SQLException {
        final var failure = failAction();
        final var restoreFailure = new SQLException("restore failed");
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(this::execute).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void restorationFailureAfterCommitDoesNotAttemptRollback() throws SQLException {
        final var failure = new SQLException("restore failed");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(this::execute)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void closeFailureAfterCommitDoesNotAttemptRollback() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(this::execute)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void restoresOriginallyDisabledAutoCommit() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        execute();

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void startingTransactionFailureClosesWithoutRunningAction() throws SQLException {
        final var failure = new SQLException("cannot start transaction");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(this::execute)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(action);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void connectionAcquisitionFailureDoesNotRunAction() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(this::execute)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(action, connection);
    }

    private DataAccessException failAction() {
        final var failure = new DataAccessException("action failed");
        doThrow(failure).when(action).run();
        return failure;
    }

    private void execute() {
        transactionTemplate.executeWithoutResult(action);
    }
}

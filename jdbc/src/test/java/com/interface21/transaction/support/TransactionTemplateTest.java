package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void bindsConnectionAndCommitsBeforeRestoringAndClosing() throws SQLException {
        final var action = mock(Runnable.class);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(action).run();

        transactionTemplate.execute(action);

        final var order = inOrder(connection, action);
        order.verify(connection).setAutoCommit(false);
        order.verify(action).run();
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void restoresOriginalAutoCommitWhenItWasAlreadyFalse() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        transactionTemplate.execute(() -> {});

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollsBackAndPreservesWorkFailure() throws SQLException {
        final var exception = new IllegalArgumentException("Work failed");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw exception; }))
                .isSameAs(exception);

        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollsBackForError() throws SQLException {
        final var error = new AssertionError("Work failed");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw error; }))
                .isSameAs(error);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesRollbackAndReleaseFailuresWithoutEnablingAutoCommit() throws SQLException {
        final var workException = new IllegalArgumentException("Work failed");
        final var rollbackException = new SQLException("Rollback failed");
        final var closeException = new SQLException("Close failed");
        doThrow(rollbackException).when(connection).rollback();
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw workException; }))
                .isSameAs(workException)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(2);
                    assertThat(exception.getSuppressed()[0]).isSameAs(rollbackException);
                    assertThat(exception.getSuppressed()[1]).hasCause(closeException);
                });

        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesRestoreFailureAfterRollbackAndStillCloses() throws SQLException {
        final var workException = new IllegalArgumentException("Work failed");
        final var restoreException = new SQLException("Restore failed");
        doThrow(restoreException).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw workException; }))
                .isSameAs(workException)
                .hasSuppressedException(restoreException);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void doesNotRollbackWhenRestoreFailsAfterCommit() throws SQLException {
        final var restoreException = new SQLException("Restore failed");
        doThrow(restoreException).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasCause(restoreException);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void unbindsAndClosesWhenStartingTransactionFails() throws SQLException {
        final var exception = new SQLException("Cannot disable auto commit");
        final var action = mock(Runnable.class);
        doThrow(exception).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> transactionTemplate.execute(action))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verifyNoInteractions(action);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rejectsNestedTransactionWithoutChangingExistingConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var action = mock(Runnable.class);

        assertThatThrownBy(() -> transactionTemplate.execute(action))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(action);
        verify(dataSource, never()).getConnection();
        verifyNoInteractions(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

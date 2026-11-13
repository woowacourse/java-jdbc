package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @AfterEach
    void assertResourceWasReleased() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void commitsResultAndRestoresConnectionBeforeClosing() throws SQLException {
        final var result = transactionTemplate.execute(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return "result";
        });

        assertThat(result).isEqualTo("result");
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackAndPreservesTheOriginalRuntimeException() throws SQLException {
        final var failure = new IllegalStateException("business failure");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw failure; }))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackAndPreservesErrors() throws SQLException {
        final var failure = new AssertionError("business error");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw failure; }))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void rollsBackWhenCommitFailsAndPreservesTheSqlCause() throws SQLException {
        final var failure = new SQLException("commit failure");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> "result"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollbackFailureDoesNotRestoreAutoCommitAndCommitPendingChanges() throws SQLException {
        final var original = new IllegalStateException("business failure");
        final var rollbackFailure = new SQLException("rollback failure");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw original; }))
                .isSameAs(original);

        assertThat(original.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void preservesTheOriginalFailureWhenRollbackThrowsAnUncheckedException() throws SQLException {
        final var original = new IllegalStateException("business failure");
        final var rollbackFailure = new UnsupportedOperationException("rollback failure");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw original; }))
                .isSameAs(original);

        assertThat(original.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void preservesTheOriginalFailureWhenRestoringAutoCommitThrowsAnError() throws SQLException {
        final var original = new IllegalStateException("business failure");
        final var restoreFailure = new AssertionError("restore failure");
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw original; }))
                .isSameAs(original);

        assertThat(original.getSuppressed()).containsExactly(restoreFailure);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void preservesTheOriginalFailureWhenConnectionReleaseAlsoFails() throws SQLException {
        final var original = new IllegalArgumentException("business failure");
        final var closeFailure = new SQLException("close failure");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> { throw original; }))
                .isSameAs(original);

        assertThat(original.getSuppressed()).hasSize(1);
        assertThat(original.getSuppressed()[0])
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
        verify(connection).close();
    }

    @Test
    void closesTheConnectionWhenBeginningTheTransactionFails() throws SQLException {
        final var failure = new SQLException("begin failure");
        final var called = new AtomicBoolean();
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> called.getAndSet(true)))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        assertThat(called).isFalse();
        verify(connection).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void preservesAnOriginallyDisabledAutoCommitSetting() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        transactionTemplate.execute(() -> "result");

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void joinsTheCallersConnectionWithoutEndingTheCallersTransaction() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(transactionTemplate.execute(() -> "result")).isEqualTo("result");
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            verify(dataSource, never()).getConnection();
            verify(connection, never()).commit();
            verify(connection, never()).rollback();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

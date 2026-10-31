package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final TransactionTemplate transactionTemplate = new TransactionTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void bindsConnectionDuringCallbackAndCommits() throws SQLException {
        final var result = transactionTemplate.execute(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return "done";
        });

        assertThat(result).isEqualTo("done");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void executeWithoutResultRunsInsideTransaction() throws SQLException {
        transactionTemplate.executeWithoutResult(() ->
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection));

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void rollsBackAndRethrowsRuntimeException() throws SQLException {
        final var failure = new DataAccessException("callback failed");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            throw failure;
        })).isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollsBackOnError() throws SQLException {
        final var failure = new AssertionError("callback failed");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            throw failure;
        })).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void commitFailureRollsBackAndTranslates() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> null))
                .isInstanceOf(TransactionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void rollbackFailureClosesWithoutRestoringAutoCommit() throws SQLException {
        final var failure = new DataAccessException("callback failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            throw failure;
        })).isSameAs(failure).hasSuppressedException(rollbackFailure);

        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void beginFailureReleasesConnectionWithoutBinding() throws SQLException {
        final var failure = new SQLException("autoCommit failed");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> null))
                .isInstanceOf(TransactionException.class).hasCause(failure);

        verify(connection).close();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void connectionFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> transactionTemplate.execute(() -> null))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
    }

    @Test
    void joinsActiveTransactionWithoutCommitting() throws SQLException {
        final var outer = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, outer);

        final var result = transactionTemplate.execute(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(outer);
            return "joined";
        });

        assertThat(result).isEqualTo("joined");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(outer);
        verify(dataSource, never()).getConnection();
        verify(outer, never()).commit();
        verify(outer, never()).close();
    }

    @Test
    void nestedExecuteCommitsOnlyOnce() throws SQLException {
        transactionTemplate.execute(() -> transactionTemplate.execute(() -> "inner"));

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
    }
}

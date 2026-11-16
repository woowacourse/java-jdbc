package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Runnable action = mock(Runnable.class);
    private final JdbcTransactionTemplate transactionTemplate = new JdbcTransactionTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @Test
    void commitFailureRollsBackBeforeRestoringAutoCommit() throws SQLException {
        final var cause = new SQLException("commit failed");
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> transactionTemplate.execute(action))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollbackFailurePreservesOriginalExceptionAndClosesConnection() throws SQLException {
        final var cause = new IllegalStateException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(cause).when(action).run();
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(action)).isSameAs(cause);

        assertThat(cause.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).commit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void autoCommitRestoreFailureAfterCommitKeepsTransactionSuccessful() throws SQLException {
        doThrow(new SQLException("restore failed")).when(connection).setAutoCommit(true);

        assertThatCode(() -> transactionTemplate.execute(action))
                .doesNotThrowAnyException();

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void closeFailureAfterCommitKeepsTransactionSuccessful() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        assertThatCode(() -> transactionTemplate.execute(action))
                .doesNotThrowAnyException();

        verify(connection).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void nestedCallLeavesCommitAndCloseToTheOuterTransaction() throws SQLException {
        final var nested = new JdbcTransactionTemplate(dataSource);

        transactionTemplate.execute(() -> {
            nested.execute(() -> assertThat(TransactionSynchronizationManager.getResource(dataSource))
                    .isSameAs(connection));
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        });

        verify(dataSource).getConnection();
        verify(connection).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void caughtNestedFailureStillRollsBackTheOuterTransaction() throws SQLException {
        final var nested = new JdbcTransactionTemplate(dataSource);
        final var cause = new IllegalStateException("nested action failed");
        doThrow(cause).when(action).run();

        assertThatThrownBy(() -> transactionTemplate.execute(() ->
                assertThatThrownBy(() -> nested.execute(action)).isSameAs(cause)))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("rollback-only");

        verify(connection, never()).commit();
        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceConnectionTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService delegate = mock(UserService.class);
    private final TxUserService service = new TxUserService(dataSource, delegate);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @Test
    void restoresAutoCommitAfterCommit() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "after", "operator");
        service.changePassword(1L, "after", "operator");
        var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void restoresAutoCommitAfterSuccessfulRollback() throws SQLException {
        doThrow(new DataAccessException("history failed"))
                .when(delegate).changePassword(1L, "after", "operator");
        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "after", "operator"));
        var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void doesNotRestoreAutoCommitWhenRollbackFails() throws SQLException {
        var original = new DataAccessException("history failed");
        var rollbackFailure = new SQLException("rollback failed");
        doThrow(original).when(delegate).changePassword(1L, "after", "operator");
        doThrow(rollbackFailure).when(connection).rollback();
        var thrown = assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "after", "operator"));
        assertThat(thrown).isSameAs(original);
        assertThat(thrown.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollsBackWhenParticipatingOperationMarksRollbackOnly() throws SQLException {
        final var failingDelegate = mock(UserService.class);
        doThrow(new DataAccessException("history failed"))
                .when(failingDelegate).changePassword(1L, "after", "operator");
        final var innerService = new TxUserService(dataSource, failingDelegate);
        doAnswer(invocation -> {
            assertThrows(DataAccessException.class, () -> innerService.changePassword(1L, "after", "operator"));
            assertThat(TransactionSynchronizationManager.isRollbackOnly(dataSource)).isTrue();
            return null;
        }).when(delegate).changePassword(1L, "after", "operator");

        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "after", "operator"));

        verify(dataSource, times(1)).getConnection();
        verify(connection, never()).commit();
        var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.isRollbackOnly(dataSource)).isFalse();
    }
}

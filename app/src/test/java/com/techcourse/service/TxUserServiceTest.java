package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private TxUserService service;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        service = new TxUserService(dataSource, delegate);
    }

    @Test
    void commitsAndRestoresConnectionAfterBusinessLogic() throws SQLException {
        service.changePassword(1L, "newPassword", "admin");

        final var order = inOrder(connection, delegate);
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(1L, "newPassword", "admin");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void commitFailureRollsBackAndReleasesResource() throws SQLException {
        final var cause = new SQLException("commit failed");
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void startFailureClosesAndUnbindsWithoutExecutingBusinessLogic() throws SQLException {
        final var cause = new SQLException("begin failed");
        doThrow(cause).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(delegate, never()).changePassword(1L, "newPassword", "admin");
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollbackAndCloseFailuresDoNotHideBusinessFailureOrEnableAutoCommit() throws SQLException {
        final var cause = new DataAccessException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(cause).when(delegate).changePassword(1L, "newPassword", "admin");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin")).isSameAs(cause);

        assertThat(cause.getSuppressed()).hasSize(2);
        assertThat(cause.getSuppressed()[0]).isSameAs(rollbackFailure);
        assertThat(cause.getSuppressed()[1]).isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void restorationFailureStillClosesAndUnbindsConnection() throws SQLException {
        final var cause = new SQLException("restore failed");
        doThrow(cause).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void restorationAndCloseFailuresDoNotHideBusinessFailure() throws SQLException {
        final var cause = new DataAccessException("history failed");
        final var restoreFailure = new SQLException("restore failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(cause).when(delegate).changePassword(1L, "newPassword", "admin");
        doThrow(restoreFailure).when(connection).setAutoCommit(true);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin")).isSameAs(cause);

        assertThat(cause.getSuppressed()).hasSize(2);
        assertThat(cause.getSuppressed()[0]).isInstanceOf(DataAccessException.class).hasCause(restoreFailure);
        assertThat(cause.getSuppressed()[1]).isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(closeFailure);
        verify(connection).rollback();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void restoresOriginalAutoCommitSetting() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        service.changePassword(1L, "newPassword", "admin");

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void existingTransactionIsNotOverwrittenOrClosed() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "admin"))
                    .isInstanceOf(IllegalStateException.class);

            verify(dataSource, never()).getConnection();
            verify(connection, never()).close();
            verify(delegate, never()).changePassword(1L, "newPassword", "admin");
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

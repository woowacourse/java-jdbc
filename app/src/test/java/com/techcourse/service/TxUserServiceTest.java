package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    @Test
    void beginFailureReleasesConnectionAndUnbindsResource() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var delegate = mock(UserService.class);
        final var cause = new SQLException("begin failed");
        when(dataSource.getConnection()).thenReturn(connection);
        doThrow(cause).when(connection).setAutoCommit(false);
        final var service = new TxUserService(dataSource, delegate);

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(delegate, never()).changePassword(1L, "newPassword", "gugu");
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollbackAndCloseFailuresPreserveOriginalExceptionAndUnbindResource() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var delegate = mock(UserService.class);
        final var cause = new DataAccessException();
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        doThrow(cause).when(delegate).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();
        final var service = new TxUserService(dataSource, delegate);

        assertThatThrownBy(() -> service.changePassword(1L, "newPassword", "gugu")).isSameAs(cause);

        assertThat(cause.getSuppressed()).hasSize(2);
        assertThat(cause.getSuppressed()[0]).isSameAs(rollbackFailure);
        assertThat(cause.getSuppressed()[1]).hasCause(closeFailure);
        verify(connection, never()).commit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

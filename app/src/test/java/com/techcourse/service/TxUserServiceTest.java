package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private Connection connection;
    private UserService delegate;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        userService = new TxUserService(delegate, dataSource);
    }

    @Test
    void changePassword_RuntimeExceptionAndRollbackFailure_PreservesOriginalExceptionAndSuppressedFailure()
            throws SQLException {
        final var originalException = new IllegalStateException("History save failed");
        final var rollbackException = new SQLException("Rollback failed");
        doThrow(originalException).when(delegate).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackException).when(connection).rollback();

        final var thrown = catchThrowable(() -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(thrown).isSameAs(originalException);
        assertThat(thrown.getSuppressed()).containsExactly(rollbackException);
        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void changePassword_CommitAndRollbackFailure_PreservesSqlCauseAndSuppressedFailure()
            throws SQLException {
        final var commitException = new SQLException("Commit failed");
        final var rollbackException = new SQLException("Rollback failed");
        doThrow(commitException).when(connection).commit();
        doThrow(rollbackException).when(connection).rollback();

        final var thrown = catchThrowable(() -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(thrown).isInstanceOf(DataAccessException.class).hasCause(commitException);
        assertThat(thrown.getCause().getSuppressed()).containsExactly(rollbackException);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).rollback();
        verify(connection).close();
    }
}

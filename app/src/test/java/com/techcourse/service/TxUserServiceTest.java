package com.techcourse.service;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService userService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userService = mock(UserService.class);

        when(dataSource.getConnection()).thenReturn(connection);

        txUserService = new TxUserService(dataSource, userService);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void commit() throws SQLException {
        txUserService.changePassword(1L, "password", "gugu");

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollbackWhenCommitFailed() throws SQLException {
        final var cause = new SQLException();
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "password", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void keepOriginalExceptionWhenRollbackFailed() throws SQLException {
        final var cause = new DataAccessException();
        final var rollbackException = new SQLException();
        doThrow(cause).when(userService).changePassword(anyLong(), anyString(), anyString());
        doThrow(rollbackException).when(connection).rollback();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "password", "gugu"))
                .isSameAs(cause)
                .hasSuppressedException(rollbackException);
        verify(connection).close();
    }
}

package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private final UserService delegate = mock(UserService.class);
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService userService = new TxUserService(delegate, dataSource);

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
    void preservesWorkFailureWhenReleaseAlsoFails() throws SQLException {
        final var workException = new IllegalArgumentException("Password change failed");
        final var closeException = new SQLException("Connection close failed");
        doThrow(workException).when(delegate).changePassword(1L, "new-password", "admin");
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(workException)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeException);
                });

        verify(connection).rollback();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesCommitFailureWhenReleaseAlsoFails() throws SQLException {
        final var commitException = new SQLException("Commit failed");
        final var closeException = new SQLException("Connection close failed");
        doThrow(commitException).when(connection).commit();
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(commitException)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeException);
                });

        verify(connection).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void reportsReleaseFailureAfterCommitWithoutRollingBack() throws SQLException {
        final var closeException = new SQLException("Connection close failed");
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeException);

        verify(connection).commit();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

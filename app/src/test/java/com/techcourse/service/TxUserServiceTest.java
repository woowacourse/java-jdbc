package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        userService = new TxUserService(delegate, dataSource);
    }

    @AfterEach
    void verifyResourceReleased() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            if (TransactionSynchronizationManager.getResource(dataSource) != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }

    @Test
    void commitsAfterDelegateCompletesAndRestoresConnection() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "newPassword", "gugu");

        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection, delegate);
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(1L, "newPassword", "gugu");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(dataSource).getConnection();
        verify(connection, never()).rollback();
    }

    @Test
    void runtimeFailureRollsBackAndPreservesOriginalException() throws SQLException {
        final var failure = new IllegalStateException("history failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void commitFailureRollsBackAndPreservesSqlException() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollbackAndCloseFailuresAreSuppressedWithoutEnablingAutoCommit() throws SQLException {
        final var failure = new IllegalStateException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure)
                .hasSuppressedException(rollbackFailure)
                .hasSuppressedException(closeFailure);

        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    void connectionAcquisitionFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(delegate, never()).changePassword(1L, "newPassword", "gugu");
    }

    @Test
    void closeFailureAfterCommitPreservesCause() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void transactionStartFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("cannot start transaction");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(delegate, never()).changePassword(1L, "newPassword", "gugu");
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void restorationFailureAfterCommitStillUnbindsAndClosesConnection() throws SQLException {
        final var failure = new SQLException("cannot restore autoCommit");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).commit();
        verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void errorRollsBackAndReleasesConnection() throws SQLException {
        final var failure = new AssertionError("delegate failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void delegatesFindAndSave() throws SQLException {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(delegate.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
        userService.save(user);

        verify(delegate).save(user);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void defaultConstructorUsesConfiguredDataSource() {
        final var configuredDataSource = DataSourceConfig.getInstance();
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(configuredDataSource)).isNotNull();
            return null;
        }).when(delegate).changePassword(1L, "newPassword", "gugu");

        new TxUserService(delegate).changePassword(1L, "newPassword", "gugu");

        verify(delegate).changePassword(1L, "newPassword", "gugu");
        assertThat(TransactionSynchronizationManager.getResource(configuredDataSource)).isNull();
    }
}

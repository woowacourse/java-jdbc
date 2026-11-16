package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TxUserServiceTest {

    private static final long ID = 1L;
    private static final String NEW_PASSWORD = "newPassword";
    private static final String CREATED_BY = "gugu";

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService delegate = mock(UserService.class);
    private final TxUserService service = new TxUserService(dataSource, delegate);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void delegatesInsideTransactionAndCommitsBeforeRestoringAndClosing() throws SQLException {
        changePassword();

        final var order = inOrder(connection, delegate);
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(ID, NEW_PASSWORD, CREATED_BY);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource).getConnection();
    }

    @Test
    void bindsConnectionOnlyWhileDelegateRuns() {
        final List<Connection> boundDuringDelegate = new ArrayList<>();
        doAnswer(invocation -> boundDuringDelegate.add(TransactionSynchronizationManager.getResource(dataSource)))
                .when(delegate).changePassword(ID, NEW_PASSWORD, CREATED_BY);

        changePassword();

        assertThat(boundDuringDelegate).containsExactly(connection);
    }

    @Test
    void joinsExistingTransactionWithoutCommittingOrClosingIt() throws SQLException {
        final Connection outer = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, outer);
        try {
            changePassword();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        verify(delegate).changePassword(ID, NEW_PASSWORD, CREATED_BY);
        verifyNoInteractions(outer);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void readAndSaveAreDelegatedWithoutStartingTransaction() throws SQLException {
        final var user = new User(ID, "gugu", "password", "gugu@example.com");
        when(delegate.findById(ID)).thenReturn(user);

        assertThat(service.findById(ID)).isSameAs(user);
        service.save(user);

        verify(delegate).save(user);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void rollsBackRuntimeFailureBeforeRestoringAndClosing() throws SQLException {
        final var failure = failDelegate();

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackErrorAndRethrowsIt() throws SQLException {
        final var failure = new AssertionError("unexpected");
        doThrow(failure).when(delegate).changePassword(ID, NEW_PASSWORD, CREATED_BY);

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void commitFailureAttemptsRollbackAndPreservesCause() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void rollbackFailureSkipsAutoCommitRestorationAndPreservesFailures() throws SQLException {
        final var failure = failDelegate();
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure, closeFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void restorationFailureAfterRollbackDoesNotReplaceBusinessFailure() throws SQLException {
        final var failure = failDelegate();
        final var restoreFailure = new SQLException("restore failed");
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void restorationFailureAfterCommitDoesNotAttemptRollback() throws SQLException {
        final var failure = new SQLException("restore failed");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void closeFailureAfterCommitDoesNotAttemptRollback() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void restoresOriginallyDisabledAutoCommit() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        changePassword();

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void startingTransactionFailureClosesWithoutDelegating() throws SQLException {
        final var failure = new SQLException("cannot start transaction");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(delegate);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void connectionAcquisitionFailureDoesNotDelegate() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(delegate, connection);
    }

    private DataAccessException failDelegate() {
        final var failure = new DataAccessException("history failed");
        doThrow(failure).when(delegate).changePassword(ID, NEW_PASSWORD, CREATED_BY);
        return failure;
    }

    private void changePassword() {
        service.changePassword(ID, NEW_PASSWORD, CREATED_BY);
    }
}
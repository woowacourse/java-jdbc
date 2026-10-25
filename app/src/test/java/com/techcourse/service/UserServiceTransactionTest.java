package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private User user;
    private UserService userService;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        user = new User(1L, "gugu", "password", "gugu@example.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(1L)).thenReturn(user);
        userService = new UserService(dataSource, userDao, userHistoryDao);
    }

    @Test
    void commitsPasswordAndHistoryUsingOneConnection() throws SQLException {
        userService.changePassword(1L, "changed-password", "support");

        final ArgumentCaptor<UserHistory> history = ArgumentCaptor.forClass(UserHistory.class);
        final InOrder transactionOrder = inOrder(connection, userDao, userHistoryDao);
        transactionOrder.verify(connection).setAutoCommit(false);
        transactionOrder.verify(userDao).findById(1L);
        transactionOrder.verify(userDao).update(user);
        transactionOrder.verify(userHistoryDao).log(history.capture());
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
        assertThat(history.getValue().getUserId()).isEqualTo(1L);
        assertThat(history.getValue().getPassword()).isEqualTo("changed-password");
        assertThat(history.getValue().getCreateBy()).isEqualTo("support");
        verify(dataSource).getConnection();
        verify(connection, never()).rollback();
    }

    @AfterEach
    void verifiesTransactionResourceWasRemoved() {
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void bindsConnectionWhileExecutingBusinessLogic() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return user;
        }).when(userDao).findById(1L);

        userService.changePassword(1L, "changed-password", "support");

        verify(connection).commit();
    }

    @Test
    void releasesConnectionWhenTransactionSetupFails() throws SQLException {
        final var failure = new SQLException("Cannot disable auto-commit");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(userDao, userHistoryDao);
        verify(connection).close();
    }

    @Test
    void closeFailureDoesNotReplaceOriginalFailure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        final var closeFailure = new SQLException("Close failed");
        doThrow(failure).when(userHistoryDao).log(any(UserHistory.class));
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).extracting(Throwable::getCause).containsExactly(closeFailure);
        verifyRollbackAndClose();
    }

    @Test
    void rollsBackAndPreservesHistoryFailure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        doThrow(failure).when(userHistoryDao).log(any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isSameAs(failure);

        verify(connection, never()).commit();
        verifyRollbackAndClose();
    }

    @Test
    void rollsBackWhenCommitFails() throws SQLException {
        final var failure = new SQLException("Commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyRollbackAndClose();
    }

    @Test
    void rollbackFailureDoesNotRestoreAutoCommitOrReplaceOriginalFailure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(failure).when(userHistoryDao).log(any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection).rollback();
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void autoCommitRestoreFailureDoesNotReplaceOriginalFailure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        final var restoreFailure = new SQLException("Auto-commit restore failed");
        doThrow(failure).when(userHistoryDao).log(any(UserHistory.class));
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verifyRollbackAndClose();
    }

    @Test
    void restoresOriginalAutoCommitSetting() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        userService.changePassword(1L, "changed-password", "support");

        final InOrder cleanupOrder = inOrder(connection);
        cleanupOrder.verify(connection).commit();
        cleanupOrder.verify(connection).setAutoCommit(false);
        cleanupOrder.verify(connection).close();
        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    void wrapsConnectionAcquisitionFailureBeforeCallingDaos() throws SQLException {
        final var failure = new SQLException("Connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "changed-password", "support"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(userDao, userHistoryDao);
        verify(connection, never()).close();
    }

    private void verifyRollbackAndClose() throws SQLException {
        final InOrder cleanupOrder = inOrder(connection);
        cleanupOrder.verify(connection).rollback();
        cleanupOrder.verify(connection).setAutoCommit(true);
        cleanupOrder.verify(connection).close();
    }
}

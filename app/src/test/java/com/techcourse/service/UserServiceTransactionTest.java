package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTransactionTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao historyDao = mock(UserHistoryDao.class);
    private final UserService service = new UserService(dataSource, userDao, historyDao);
    private final User user = new User(1L, "gugu", "password", "gugu@example.com");

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
    }

    @Test
    void sharesConnectionAndCommitsBeforeRestoringAndClosing() throws SQLException {
        changePassword();

        final var order = inOrder(connection, userDao, historyDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(historyDao).log(eq(connection), any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource).getConnection();
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void doesNotUseDaoMethodsThatOpenSeparateConnections() {
        changePassword();

        verify(userDao).findById(connection, 1L);
        verify(userDao).update(connection, user);
        verify(historyDao).log(eq(connection), any(UserHistory.class));
        verifyNoMoreInteractions(userDao, historyDao);
    }

    @Test
    void lookupFailureRollsBackWithoutWriting() throws SQLException {
        final var failure = new DataAccessException("lookup failed");
        when(userDao.findById(connection, 1L)).thenThrow(failure);

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        verify(userDao, never()).update(any(Connection.class), any(User.class));
        verifyNoInteractions(historyDao);
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackRuntimeFailureBeforeRestoringAndClosing() throws SQLException {
        final var failure = failHistory();

        assertThatThrownBy(this::changePassword).isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
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
        final var failure = failHistory();
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
        final var failure = failHistory();
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
    void startingTransactionFailureClosesWithoutWriting() throws SQLException {
        final var failure = new SQLException("cannot start transaction");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(this::changePassword)
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(userDao, historyDao);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    private DataAccessException failHistory() {
        final var failure = new DataAccessException("history failed");
        doThrow(failure).when(historyDao).log(eq(connection), any(UserHistory.class));
        return failure;
    }

    private void changePassword() {
        service.changePassword(1L, "newPassword", "gugu");
    }
}

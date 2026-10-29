package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
    }

    @Test
    void commitsSharedConnectionThenRestoresAndClosesIt() throws SQLException {
        service.changePassword(1L, "new-password", "admin");

        final var order = inOrder(connection, userDao, historyDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(historyDao).log(eq(connection), any());
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource).getConnection();
    }

    @Test
    void runtimeFailureRollsBackBeforeRestoringAndClosing() throws SQLException {
        final var cause = new DataAccessException("history failed");
        doThrow(cause).when(historyDao).log(eq(connection), any());

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin")).isSameAs(cause);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void commitFailureIsTranslatedAfterRollback() throws SQLException {
        final var cause = new SQLException("commit failed");
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void rollbackFailurePreservesOriginalFailureAndDoesNotEnableAutoCommit() throws SQLException {
        final var cause = new DataAccessException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(cause).when(historyDao).log(eq(connection), any());
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isSameAs(cause).hasSuppressedException(rollbackFailure);

        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void restorationAndCloseFailuresDoNotReplaceOriginalFailure() throws SQLException {
        final var cause = new DataAccessException("history failed");
        final var restorationFailure = new SQLException("restore failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(cause).when(historyDao).log(eq(connection), any());
        doThrow(restorationFailure).when(connection).setAutoCommit(true);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isSameAs(cause).hasSuppressedException(restorationFailure).hasSuppressedException(closeFailure);

        verify(connection).rollback();
    }

    @Test
    void connectionFailureIsTranslatedWithoutCallingDaos() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyNoInteractions(userDao, historyDao);
    }

    @Test
    void restorationFailureAfterCommitIsReportedWithoutRollback() throws SQLException {
        final var cause = new SQLException("restore failed");
        doThrow(cause).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void closesConnectionWhenTransactionSetupFails() throws SQLException {
        final var cause = new SQLException("setup failed");
        doThrow(cause).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> service.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyNoInteractions(userDao, historyDao);
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }
}

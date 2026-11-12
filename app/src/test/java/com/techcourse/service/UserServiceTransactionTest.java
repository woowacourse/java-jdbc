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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao historyDao = mock(UserHistoryDao.class);
    private final UserService userService = new UserService(dataSource, userDao, historyDao);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L))
                .thenReturn(new User(1L, "gugu", "password", "gugu@example.com"));
    }

    @Test
    void commitFailureRollsBackBeforeRestoringAutoCommit() throws SQLException {
        final var cause = new SQLException("commit failed");
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void rollbackFailurePreservesOriginalExceptionAndClosesConnection() throws SQLException {
        final var cause = new IllegalStateException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(cause).when(historyDao).log(eq(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed", "admin")).isSameAs(cause);

        assertThat(cause.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).commit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void autoCommitRestoreFailureAfterCommitKeepsPasswordChangeSuccessful() throws SQLException {
        doThrow(new SQLException("restore failed")).when(connection).setAutoCommit(true);

        assertThatCode(() -> userService.changePassword(1L, "changed", "admin"))
                .doesNotThrowAnyException();

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void closeFailureAfterCommitKeepsPasswordChangeSuccessful() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        assertThatCode(() -> userService.changePassword(1L, "changed", "admin"))
                .doesNotThrowAnyException();

        verify(connection).commit();
    }
}

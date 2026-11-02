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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceConnectionTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao historyDao = mock(UserHistoryDao.class);
    private final UserService service = new UserService(dataSource, userDao, historyDao);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(1L)).thenReturn(new User(1L, "user", "before", "user@example.com"));
    }

    @Test
    void restoresAutoCommitAfterCommit() throws SQLException {
        service.changePassword(1L, "after", "operator");
        var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void restoresAutoCommitAfterSuccessfulRollback() throws SQLException {
        doThrow(new DataAccessException("history failed")).when(historyDao).log(any(Connection.class), any());
        assertThrows(DataAccessException.class, () -> service.changePassword(1L, "after", "operator"));
        var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    void doesNotRestoreAutoCommitWhenRollbackFails() throws SQLException {
        var original = new DataAccessException("history failed");
        var rollbackFailure = new SQLException("rollback failed");
        doThrow(original).when(historyDao).log(any(Connection.class), any());
        doThrow(rollbackFailure).when(connection).rollback();
        var thrown = assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "after", "operator"));
        assertThat(thrown).isSameAs(original);
        assertThat(thrown.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }
}

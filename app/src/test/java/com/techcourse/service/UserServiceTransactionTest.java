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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        final var userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(connection, 1L))
                .thenReturn(new User(1L, "gugu", "password", "gugu@example.com"));
        userService = new UserService(userDao, userHistoryDao, dataSource);
    }

    @Test
    void closesConnectionAfterCommit() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    void closesConnectionAfterRollbackAndPreservesOriginalException() throws SQLException {
        final var failure = new DataAccessException("이력 저장 실패");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void rollsBackAndClosesConnectionWhenCommitFails() throws SQLException {
        final var failure = new SQLException("커밋 실패");
        doThrow(failure).when(connection).commit();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(failure);
        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).close();
    }

    @Test
    void preservesRollbackFailureAndStillClosesConnection() throws SQLException {
        final var failure = new DataAccessException("이력 저장 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(failure);
        assertThat(actual.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection).close();
        verify(connection, never()).commit();
    }
}

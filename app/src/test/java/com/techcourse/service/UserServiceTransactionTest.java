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
    private final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
    private final UserService userService = new UserService(dataSource, userDao, userHistoryDao);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(connection, 1L)).thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void commitsAfterUpdateAndHistoryOnOneConnection() throws SQLException {
        userService.changePassword(1L, "changed", "gugu");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).update(eq(connection), any(User.class));
        order.verify(userHistoryDao).log(eq(connection), any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackWhenHistoryFails() throws SQLException {
        doThrow(new DataAccessException("history failed")).when(userHistoryDao).log(eq(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "changed", "gugu"))
                .isInstanceOf(DataAccessException.class).hasMessage("history failed");

        verify(userDao).update(eq(connection), any(User.class));
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    void commitFailureRollsBackAndTranslates() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "changed", "gugu"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).rollback();
        verify(connection).close();
    }
}

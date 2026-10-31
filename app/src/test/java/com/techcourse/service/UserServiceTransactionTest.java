package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class UserServiceTransactionTest {

    @Test
    void testTransactionRollbackWhenCommitFails() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var userDao = mock(UserDao.class);
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var commitFailure = new SQLException("커밋 실패");
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(1L, connection))
                .thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        doThrow(commitFailure).when(connection).commit();
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(commitFailure);
        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).close();
    }

    @Test
    void testTransactionRollbackFailurePreservesOriginalException() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var userDao = mock(UserDao.class);
        final var userHistoryDao = mock(UserHistoryDao.class);
        final var originalFailure = new DataAccessException("이력 저장 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(1L, connection))
                .thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        doThrow(originalFailure).when(userHistoryDao).log(any(UserHistory.class), eq(connection));
        doThrow(rollbackFailure).when(connection).rollback();
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(originalFailure);
        assertThat(actual.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).commit();
        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).close();
    }
}

package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.InOrder;

class UserServiceTransactionTest {

    @Test
    void testTransactionRollbackWhenCommitFails() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final UserDao userDao = mock(UserDao.class);
        final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
        final SQLException commitFailure = new SQLException("커밋 실패");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(1L))
                .thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        doThrow(commitFailure).when(connection).commit();
        final UserService userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final DataAccessException actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).hasCause(commitFailure);
        final InOrder order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).close();
    }

    @Test
    void testTransactionRollbackFailurePreservesOriginalException() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final UserDao userDao = mock(UserDao.class);
        final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
        final DataAccessException originalFailure = new DataAccessException("이력 저장 실패");
        final SQLException rollbackFailure = new SQLException("롤백 실패");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(1L))
                .thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
        doThrow(originalFailure).when(userHistoryDao).log(any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();
        final UserService userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final DataAccessException actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual).isSameAs(originalFailure);
        assertThat(actual.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).commit();
        final InOrder order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).close();
    }
}

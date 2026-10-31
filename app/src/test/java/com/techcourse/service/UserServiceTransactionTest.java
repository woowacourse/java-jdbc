package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(1L)).thenReturn(user);
        userService = new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));
    }

    @Test
    void commitsAfterBothWritesAndRestoresConnection() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(1L);
        order.verify(userDao).update(user);
        order.verify(userHistoryDao).log(any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void commitFailureRollsBackAndClosesConnection() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollbackFailurePreservesOriginalFailureWithoutEnablingAutoCommit() throws SQLException {
        final var failure = new IllegalStateException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(failure).when(userHistoryDao).log(any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure)
                .hasSuppressedException(rollbackFailure);

        verify(connection, never()).commit();
        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
    @Test
    void existingTransactionIsNotReplacedOrClosed() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

}

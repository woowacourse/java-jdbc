package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserDao userDao = mock(UserDao.class);
    private final UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
    private final UserService userService = new TxUserService(dataSource, new AppUserService(userDao, userHistoryDao));

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(userDao.findById(1L)).thenReturn(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void changePasswordRunsUpdateAndHistoryInOneTransaction() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(userHistoryDao).log(any(UserHistory.class));

        userService.changePassword(1L, "changed", "gugu");

        final var order = inOrder(connection, userDao, userHistoryDao);
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).update(any(User.class));
        order.verify(userHistoryDao).log(any(UserHistory.class));
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void rollsBackWhenHistoryFails() throws SQLException {
        doThrow(new DataAccessException("history failed")).when(userHistoryDao).log(any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "changed", "gugu"))
                .isInstanceOf(DataAccessException.class).hasMessage("history failed");

        verify(userDao).update(any(User.class));
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void findByIdDelegatesInsideTransaction() throws SQLException {
        assertThat(userService.findById(1L).getAccount()).isEqualTo("gugu");

        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void insertDelegatesInsideTransaction() throws SQLException {
        final var user = new User("new", "password", "new@example.com");

        userService.insert(user);

        verify(userDao).insert(user);
        verify(connection).commit();
        verify(connection).close();
    }
}

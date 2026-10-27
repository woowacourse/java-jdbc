package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceExceptionTest {

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
        when(userDao.findById(1L))
                .thenReturn(Optional.of(new User(1L, "gugu", "password", "gugu@example.com")));

        userService = new UserService(dataSource, userDao, userHistoryDao);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void preservesOriginalExceptionWhenRollbackFails() throws SQLException {
        final var original = failHistorySave();
        final var rollbackFailure = new SQLException("롤백 실패");
        doThrow(rollbackFailure).when(connection).rollback();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual.getCause()).isSameAs(original);
        assertThat(original.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void preservesOriginalExceptionWhenCloseFails() throws SQLException {
        final var original = failHistorySave();
        final var closeFailure = new SQLException("연결 종료 실패");
        doThrow(closeFailure).when(connection).close();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual.getCause()).isSameAs(original);
        assertThat(actual.getSuppressed()).containsExactly(closeFailure);
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void preservesBothRollbackAndCloseFailures() throws SQLException {
        final var original = failHistorySave();
        final var rollbackFailure = new SQLException("롤백 실패");
        final var closeFailure = new SQLException("연결 종료 실패");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual.getCause()).isSameAs(original);
        assertThat(original.getSuppressed()).containsExactly(rollbackFailure);
        assertThat(actual.getSuppressed()).containsExactly(closeFailure);
        verify(connection, never()).commit();
    }

    @Test
    void propagatesCloseFailureAfterCommit() throws SQLException {
        final var closeFailure = new SQLException("연결 종료 실패");
        doThrow(closeFailure).when(connection).close();

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual.getCause()).isSameAs(closeFailure);
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    private DataAccessException failHistorySave() {
        final var original = new DataAccessException("이력 저장 실패");
        doThrow(original).when(userHistoryDao).log(any(UserHistory.class));
        return original;
    }
}

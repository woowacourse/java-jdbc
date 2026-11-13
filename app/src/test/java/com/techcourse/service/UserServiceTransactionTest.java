package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
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

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        userHistoryDao = mock(UserHistoryDao.class);
        userService = new UserService(dataSource, userDao, userHistoryDao);
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
    }

    @Test
    @DisplayName("조회와 변경, 이력 저장에 같은 커넥션을 사용하고 커밋한다")
    void commit_with_shared_connection() throws SQLException {
        userService.changePassword(1L, "newPassword", "gugu");

        verify(userDao).findById(connection, 1L);
        verify(userDao).updatePassword(eq(connection), any(User.class));
        verify(userHistoryDao).log(eq(connection), any(UserHistory.class));

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("원래 autoCommit이 꺼져 있었다면 커밋 후에도 그 상태로 복구한다")
    void restore_original_auto_commit() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    @DisplayName("이력 저장이 실패하면 명시적으로 롤백하고 처음 예외를 그대로 던진다")
    void rollback_history_failure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    @DisplayName("비밀번호 변경 중 Error가 발생해도 롤백하고 같은 Error를 던진다")
    void rollback_error() throws SQLException {
        final var failure = new AssertionError("Password update failed");
        doThrow(failure).when(userDao).updatePassword(eq(connection), any(User.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    @DisplayName("커밋 실패 시 롤백을 시도하고 SQL 예외를 보존한다")
    void rollback_commit_failure() throws SQLException {
        final var failure = new SQLException("Commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessageContaining("userId=1")
                .hasCause(failure);

        verify(connection).rollback();
    }

    @Test
    @DisplayName("롤백 실패 시 autoCommit을 복구하지 않고 처음 예외를 보존한다")
    void preserve_rollback_failure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        final var rollbackFailure = new SQLException("Rollback failed");
        doThrow(failure).when(userHistoryDao).log(eq(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);
        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);

        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    @DisplayName("커밋 후 상태 복구가 실패하면 정리 실패 예외로 구분한다")
    void restore_failure_after_commit() throws SQLException {
        final var failure = new SQLException("Restore failed");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isExactlyInstanceOf(TransactionCleanupException.class)
                .hasMessageContaining("committed")
                .hasMessageContaining("userId=1")
                .hasCause(failure);

        verify(connection).commit();
        verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("커밋 후 커넥션 종료가 실패하면 정리 실패 예외로 구분한다")
    void close_failure_after_commit() throws SQLException {
        final var failure = new SQLException("Close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isExactlyInstanceOf(TransactionCleanupException.class)
                .hasMessageContaining("committed")
                .hasMessageContaining("userId=1")
                .hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }
}

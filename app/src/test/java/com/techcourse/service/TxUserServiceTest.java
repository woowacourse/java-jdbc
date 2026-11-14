package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        userService = new TxUserService(delegate, dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("업무 실행 동안 커넥션을 바인딩하고 커밋 뒤 해제한다")
    void commit_with_shared_connection() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "newPassword", "gugu");

        userService.changePassword(1L, "newPassword", "gugu");

        verify(delegate).changePassword(1L, "newPassword", "gugu");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

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

        verify(connection).commit();
        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    @DisplayName("업무 실행이 실패하면 롤백하고 처음 예외를 그대로 던진다")
    void rollback_delegate_failure() throws SQLException {
        final var failure = new DataAccessException("History insert failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    @DisplayName("비밀번호 변경 중 Error가 발생해도 롤백하고 같은 Error를 던진다")
    void rollback_error() throws SQLException {
        final var failure = new AssertionError("Password update failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");

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
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");
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

    @Test
    @DisplayName("조회와 저장은 트랜잭션 없이 위임한다")
    void delegate_find_and_save() {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        when(delegate.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);
        userService.save(user);

        verify(delegate).save(user);
        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("이미 바인딩된 커넥션이 있으면 트랜잭션을 거부하고 기존 연결을 유지한다")
    void reject_existing_transaction() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        verifyNoInteractions(connection, delegate);
    }

    @Test
    @DisplayName("업무 실패 뒤 커넥션 종료도 실패하면 처음 예외에 종료 실패를 보존한다")
    void preserve_close_failure() throws SQLException {
        final var failure = new DataAccessException("Password update failed");
        final var closeFailure = new SQLException("Close failed");
        doThrow(failure).when(delegate).changePassword(1L, "newPassword", "gugu");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure)
                .hasSuppressedException(closeFailure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

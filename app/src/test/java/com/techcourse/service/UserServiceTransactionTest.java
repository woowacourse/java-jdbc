package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("비밀번호 변경 트랜잭션의 경계와 예외 처리")
class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;
    private UserHistoryDao historyDao;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userDao = mock(UserDao.class);
        historyDao = mock(UserHistoryDao.class);
        user = new User(1L, "gugu", "password", "gugu@example.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        when(userDao.findById(connection, 1L)).thenReturn(user);
        userService = new UserService(dataSource, userDao, historyDao);
    }

    @Test
    @DisplayName("하나의 Connection으로 비밀번호와 이력을 저장한 뒤 커밋하고 상태를 복원해 닫는다")
    void commitsAllOperationsOnOneConnectionThenRestoresAndClosesIt() throws SQLException {
        userService.changePassword(1L, "new-password", "admin");

        final var history = ArgumentCaptor.forClass(UserHistory.class);
        final var order = inOrder(dataSource, connection, userDao, historyDao);
        order.verify(dataSource).getConnection();
        order.verify(connection).getAutoCommit();
        order.verify(connection).setAutoCommit(false);
        order.verify(userDao).findById(connection, 1L);
        order.verify(userDao).update(connection, user);
        order.verify(historyDao).log(same(connection), history.capture());
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(history.getValue().getUserId()).isEqualTo(1L);
        assertThat(history.getValue().getPassword()).isEqualTo("new-password");
        assertThat(history.getValue().getCreateBy()).isEqualTo("admin");
        verify(connection, never()).rollback();
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    @DisplayName("이력 저장 중 런타임 예외가 발생하면 롤백하고 원래 예외를 전달한다")
    void rollsBackOnUncheckedHistoryFailure() throws SQLException {
        final var failure = new IllegalStateException("이력 실패");
        doThrow(failure).when(historyDao).log(same(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("사용자 수정에 실패하면 이력을 저장하지 않고 롤백한다")
    void doesNotWriteHistoryWhenUserUpdateFails() throws SQLException {
        final var failure = new DataAccessException("수정 실패");
        doThrow(failure).when(userDao).update(connection, user);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyNoInteractions(historyDao);
        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("커밋 중 SQL 예외가 발생하면 롤백하고 원인 예외를 보존한다")
    void rollsBackWhenCommitThrowsSQLException() throws SQLException {
        final var failure = new SQLException("커밋 실패");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    @DisplayName("Connection을 얻지 못하면 DAO를 호출하지 않고 데이터 접근 예외로 변환한다")
    void translatesConnectionAcquisitionFailureWithoutCallingDaos() throws SQLException {
        final var failure = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(connection, userDao, historyDao);
    }

    @Test
    @DisplayName("트랜잭션을 시작하지 못하면 DAO를 호출하지 않고 Connection을 닫는다")
    void closesConnectionWhenTransactionCannotStart() throws SQLException {
        final var failure = new SQLException("트랜잭션 시작 실패");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(userDao, historyDao);
        verify(connection).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("롤백도 실패하면 자동 커밋을 켜지 않고 원래 예외에 롤백 예외를 추가한다")
    void preservesBusinessFailureWhenRollbackFailsWithoutEnablingAutoCommit() throws SQLException {
        final var failure = new IllegalStateException("이력 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        doThrow(failure).when(historyDao).log(same(connection), any(UserHistory.class));
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    @DisplayName("롤백 후 자동 커밋 복원에 실패하면 원래 예외에 복원 예외를 추가한다")
    void preservesBusinessFailureWhenAutoCommitRestorationFails() throws SQLException {
        final var failure = new IllegalStateException("이력 실패");
        final var restoreFailure = new SQLException("상태 복원 실패");
        doThrow(failure).when(historyDao).log(same(connection), any(UserHistory.class));
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("커밋 후 상태 복원에 실패하면 롤백 없이 원인 예외를 전달하고 Connection을 닫는다")
    void reportsRestorationFailureAfterCommitWithoutAttemptingRollback() throws SQLException {
        final var failure = new SQLException("상태 복원 실패");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    @DisplayName("자동 커밋이 처음부터 꺼져 있으면 커밋 후에도 꺼진 상태를 유지한다")
    void restoresOriginalAutoCommitMode() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        userService.changePassword(1L, "new-password", "admin");

        verify(connection, times(2)).setAutoCommit(false);
        verify(connection, never()).setAutoCommit(true);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    @DisplayName("업무 처리 중 Error가 발생해도 롤백하고 Connection을 닫는다")
    void rollsBackAndClosesConnectionOnError() throws SQLException {
        final var failure = new AssertionError("이력 실패");
        doThrow(failure).when(historyDao).log(same(connection), any(UserHistory.class));

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRolledBackAndClosed();
    }

    private void verifyRolledBackAndClosed() throws SQLException {
        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).commit();
    }
}

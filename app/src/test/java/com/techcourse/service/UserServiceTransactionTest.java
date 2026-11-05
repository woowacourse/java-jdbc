package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("사용자 트랜잭션 서비스의 경계와 자원 해제")
class UserServiceTransactionTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService delegate;
    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        delegate = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        userService = new TxUserService(delegate, dataSource);
    }

    @AfterEach
    void tearDown() {
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("업무 실행 동안 Connection을 동기화하고 성공하면 커밋한 뒤 상태를 복원해 닫는다")
    void commitsWithConnectionBoundDuringBusinessLogic() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "new-password", "admin");

        userService.changePassword(1L, "new-password", "admin");

        final var order = inOrder(dataSource, connection, delegate);
        order.verify(dataSource).getConnection();
        order.verify(connection).getAutoCommit();
        order.verify(connection).setAutoCommit(false);
        order.verify(delegate).changePassword(1L, "new-password", "admin");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        verify(dataSource, times(1)).getConnection();
    }

    @Test
    @DisplayName("업무 중 런타임 예외가 발생하면 롤백하고 원래 예외를 전달한다")
    void rollsBackOnBusinessFailure() throws SQLException {
        final var failure = new IllegalStateException("이력 실패");
        failBusiness(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("커밋 중 SQL 예외가 발생하면 롤백하고 원인을 보존한다")
    void rollsBackWhenCommitFails() throws SQLException {
        final var failure = new SQLException("커밋 실패");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        final var order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }

    @Test
    @DisplayName("연결 획득에 실패하면 업무를 호출하지 않고 원인을 보존한다")
    void doesNotCallBusinessWhenConnectionAcquisitionFails() throws SQLException {
        final var failure = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(connection, delegate);
    }

    @Test
    @DisplayName("자동 커밋 상태 조회에 실패해도 연결을 닫고 바인딩을 남기지 않는다")
    void closesConnectionWhenReadingAutoCommitFails() throws SQLException {
        final var failure = new SQLException("상태 조회 실패");
        when(connection.getAutoCommit()).thenThrow(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(delegate);
        verify(connection).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("트랜잭션 시작에 실패하면 업무를 호출하지 않고 연결을 닫는다")
    void closesConnectionWhenTransactionCannotStart() throws SQLException {
        final var failure = new SQLException("시작 실패");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(delegate);
        verify(connection).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("롤백도 실패하면 자동 커밋을 켜지 않고 원래 예외에 롤백 예외를 추가한다")
    void preservesBusinessFailureWhenRollbackFails() throws SQLException {
        final var failure = new IllegalStateException("업무 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        failBusiness(failure);
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    @DisplayName("롤백 후 상태 복원에 실패하면 원래 예외에 복원 예외를 추가한다")
    void preservesBusinessFailureWhenRestorationFails() throws SQLException {
        final var failure = new IllegalStateException("업무 실패");
        final var restoreFailure = new SQLException("복원 실패");
        failBusiness(failure);
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(restoreFailure);
        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("커밋 후 복원에 실패하면 롤백 없이 원인을 전달하고 연결을 닫는다")
    void reportsRestorationFailureAfterCommit() throws SQLException {
        final var failure = new SQLException("복원 실패");
        doThrow(failure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

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
    @DisplayName("업무 중 Error가 발생해도 롤백하고 연결을 닫는다")
    void rollsBackOnError() throws SQLException {
        final var failure = new AssertionError("업무 오류");
        failBusiness(failure);

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("업무와 연결 종료가 함께 실패하면 원래 예외에 종료 예외를 추가한다")
    void preservesBusinessFailureWhenConnectionClosingFails() throws SQLException {
        final var failure = new IllegalStateException("업무 실패");
        final var closeFailure = new SQLException("종료 실패");
        failBusiness(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
        verifyRolledBackAndClosed();
    }

    @Test
    @DisplayName("커밋 후 연결 종료에 실패하면 종료 원인을 전달하고 바인딩은 제거한다")
    void reportsConnectionClosingFailureAfterCommit() throws SQLException {
        final var failure = new SQLException("종료 실패");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("조회는 새 트랜잭션을 시작하지 않고 업무 서비스에 위임한다")
    void delegatesFindById() {
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(delegate.findById(1L)).thenReturn(user);

        assertThat(userService.findById(1L)).isSameAs(user);

        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("저장은 새 트랜잭션을 시작하지 않고 업무 서비스에 위임한다")
    void delegatesSave() {
        final var user = new User("gugu", "password", "gugu@example.com");

        userService.save(user);

        verify(delegate).save(user);
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("이미 동기화된 연결이 있으면 기존 트랜잭션에 참여하고 소유자의 연결을 유지한다")
    void joinsExistingTransactionWithoutCompletingIt() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            userService.changePassword(1L, "new-password", "admin");

            verify(delegate).changePassword(1L, "new-password", "admin");
            verifyNoInteractions(dataSource, connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("기존 트랜잭션 참여 중 실패하면 예외를 소유자에게 전달하고 연결은 유지한다")
    void leavesExistingTransactionToOwnerOnFailure() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new IllegalStateException("업무 실패");
        failBusiness(failure);
        try {
            assertThatThrownBy(() -> userService.changePassword(1L, "new-password", "admin"))
                    .isSameAs(failure);

            verifyNoInteractions(dataSource, connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void failBusiness(final Throwable failure) {
        doThrow(failure).when(delegate).changePassword(1L, "new-password", "admin");
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

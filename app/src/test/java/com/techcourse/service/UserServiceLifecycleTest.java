package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceLifecycleTest {

    private final List<DataSource> dataSources = new ArrayList<>();

    @AfterEach
    void noTransactionConnectionRemainsBound() {
        for (final var dataSource : dataSources) {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }
    }

    @Test
    @DisplayName("일반 조회와 저장은 내부 서비스에 위임하고 별도 트랜잭션 연결을 열지 않는다")
    void findAndSaveDelegateWithoutOpeningATransactionConnection() throws Exception {
        final var fixture = fixture();
        when(fixture.delegate().findById(1L)).thenReturn(fixture.user());

        assertThat(fixture.service().findById(1L)).isSameAs(fixture.user());
        fixture.service().save(fixture.user());

        verify(fixture.delegate()).findById(1L);
        verify(fixture.delegate()).save(fixture.user());
        verifyNoInteractions(fixture.dataSource(), fixture.connection());
    }

    @Test
    @DisplayName("공유 연결의 자동 커밋을 끈 뒤 업무·커밋·설정 복원·종료 순서로 실행한다")
    void successCommitsBeforeRestoringAndClosingTheSharedConnection() throws Exception {
        final var fixture = fixture();

        fixture.service().changePassword(1L, "newPassword", "operator");

        verify(fixture.dataSource(), times(1)).getConnection();
        final var order = inOrder(fixture.connection(), fixture.delegate());
        order.verify(fixture.connection()).getAutoCommit();
        order.verify(fixture.connection()).setAutoCommit(false);
        order.verify(fixture.delegate()).changePassword(1L, "newPassword", "operator");
        order.verify(fixture.connection()).commit();
        order.verify(fixture.connection()).setAutoCommit(true);
        order.verify(fixture.connection()).close();
        verify(fixture.connection(), never()).rollback();
    }

    @Test
    @DisplayName("변경 대상 사용자가 없으면 명확한 예외를 던지고 쓰기 없이 트랜잭션을 정리한다")
    void missingUserRollsBackWithoutUpdatingOrLogging() throws Exception {
        final var fixture = fixture();

        final var failure = new UserNotFoundException(404L);
        doThrow(failure).when(fixture.delegate()).changePassword(404L, "newPassword", "operator");

        final var thrown = assertThrows(UserNotFoundException.class,
                () -> fixture.service().changePassword(404L, "newPassword", "operator"));

        assertThat(thrown).isSameAs(failure);
        verify(fixture.delegate()).changePassword(404L, "newPassword", "operator");
        final var order = inOrder(fixture.connection());
        order.verify(fixture.connection()).rollback();
        order.verify(fixture.connection()).setAutoCommit(true);
        order.verify(fixture.connection()).close();
        verify(fixture.connection(), never()).commit();
    }

    @Test
    @DisplayName("업무의 RuntimeException과 Error는 원본을 유지한 채 롤백·복원·종료한다")
    void runtimeAndErrorFailuresRollBackWithoutReplacingTheOriginalFailure() throws Exception {
        for (final var failure : new Throwable[]{new DataAccessException("business"), new AssertionError("business")}) {
            final var fixture = fixture();
            doThrow(failure).when(fixture.delegate()).changePassword(1L, "newPassword", "operator");

            assertThat(assertThrows(Throwable.class,
                    () -> fixture.service().changePassword(1L, "newPassword", "operator"))).isSameAs(failure);

            final var order = inOrder(fixture.connection());
            order.verify(fixture.connection()).rollback();
            order.verify(fixture.connection()).setAutoCommit(true);
            order.verify(fixture.connection()).close();
            verify(fixture.connection(), never()).commit();
        }
    }

    @Test
    @DisplayName("롤백 실패 시 자동 커밋을 켜지 않고 롤백·종료 실패를 원래 예외에 보존한다")
    void failedRollbackSkipsRestoreAndKeepsCleanupFailuresSuppressed() throws Exception {
        final var fixture = fixture();
        final var failure = new DataAccessException("business");
        final var rollbackFailure = new SQLException("rollback");
        final var closeFailure = new SQLException("close");
        doThrow(failure).when(fixture.delegate()).changePassword(1L, "newPassword", "operator");
        doThrow(rollbackFailure).when(fixture.connection()).rollback();
        doThrow(closeFailure).when(fixture.connection()).close();

        final var thrown = assertThrows(DataAccessException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown).isSameAs(failure);
        assertThat(thrown.getSuppressed()).containsExactly(rollbackFailure, closeFailure);
        verify(fixture.connection(), never()).setAutoCommit(true);
        verify(fixture.connection(), never()).commit();
        verify(fixture.connection()).close();
    }

    @Test
    @DisplayName("롤백 후 설정 복원 실패가 업무 예외를 덮지 않는다")
    void failedRestoreAfterRollbackDoesNotHideTheBusinessFailure() throws Exception {
        final var fixture = fixture();
        final var failure = new DataAccessException("business");
        final var restoreFailure = new SQLException("restore");
        doThrow(failure).when(fixture.delegate()).changePassword(1L, "newPassword", "operator");
        doThrow(restoreFailure).when(fixture.connection()).setAutoCommit(true);

        final var thrown = assertThrows(DataAccessException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown).isSameAs(failure);
        assertThat(thrown.getSuppressed()).containsExactly(restoreFailure);
        verify(fixture.connection()).rollback();
        verify(fixture.connection()).close();
        verify(fixture.connection(), never()).commit();
    }

    @Test
    @DisplayName("커밋 실패 시 롤백·복원·종료하고 원래 SQLException을 원인으로 보존한다")
    void failedCommitRollsBackAndKeepsTheSqlCause() throws Exception {
        final var fixture = fixture();
        final var failure = new SQLException("commit");
        doThrow(failure).when(fixture.connection()).commit();

        final var thrown = assertThrows(DataAccessException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown).isExactlyInstanceOf(DataAccessException.class);
        assertThat(thrown.getCause()).isSameAs(failure);
        final var order = inOrder(fixture.connection());
        order.verify(fixture.connection()).commit();
        order.verify(fixture.connection()).rollback();
        order.verify(fixture.connection()).setAutoCommit(true);
        order.verify(fixture.connection()).close();
    }

    @Test
    @DisplayName("커밋·롤백·종료가 모두 실패해도 커밋 SQLException과 추가 원인을 보존한다")
    void failedCommitKeepsRollbackAndCloseFailuresOnItsOriginalCause() throws Exception {
        final var fixture = fixture();
        final var commitFailure = new SQLException("commit");
        final var rollbackFailure = new SQLException("rollback");
        final var closeFailure = new SQLException("close");
        doThrow(commitFailure).when(fixture.connection()).commit();
        doThrow(rollbackFailure).when(fixture.connection()).rollback();
        doThrow(closeFailure).when(fixture.connection()).close();

        final var thrown = assertThrows(DataAccessException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown.getCause()).isSameAs(commitFailure);
        assertThat(commitFailure.getSuppressed()).containsExactly(rollbackFailure, closeFailure);
        verify(fixture.connection(), never()).setAutoCommit(true);
        verify(fixture.connection()).close();
    }

    @Test
    @DisplayName("연결 획득·상태 조회·설정 실패 시 업무를 실행하지 않고 획득한 연결만 닫는다")
    void initializationFailuresNeverRunBusinessAndCloseOnlyAcquiredConnections() throws Exception {
        for (final var stage : new String[]{"get", "readAutoCommit", "setAutoCommit"}) {
            final var fixture = fixture();
            final var failure = new SQLException(stage);
            if (stage.equals("get")) {
                when(fixture.dataSource().getConnection()).thenThrow(failure);
            } else if (stage.equals("readAutoCommit")) {
                when(fixture.connection().getAutoCommit()).thenThrow(failure);
            } else {
                doThrow(failure).when(fixture.connection()).setAutoCommit(false);
            }

            final var thrown = assertThrows(DataAccessException.class,
                    () -> fixture.service().changePassword(1L, "newPassword", "operator"));

            assertThat(thrown.getCause()).isSameAs(failure);
            verifyNoInteractions(fixture.delegate());
            verify(fixture.connection(), never()).commit();
            verify(fixture.connection(), never()).rollback();
            verify(fixture.connection(), times(stage.equals("get") ? 0 : 1)).close();
        }
    }

    @Test
    @DisplayName("커밋 후 연결 종료 실패는 업무 실패와 다른 예외로 알리고 롤백하지 않는다")
    void closeFailureAfterCommitIsReportedWithoutRollback() throws Exception {
        final var fixture = fixture();
        final var failure = new SQLException("close");
        doThrow(failure).when(fixture.connection()).close();

        final var thrown = assertThrows(TransactionCleanupException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown).hasMessage("Password change was committed, but connection cleanup failed");
        assertThat(thrown.getCause()).isSameAs(failure);
        verify(fixture.connection()).commit();
        verify(fixture.connection()).setAutoCommit(true);
        verify(fixture.connection(), never()).rollback();
    }

    @Test
    @DisplayName("커밋 후 설정 복원 실패도 별도 예외로 알리며 연결을 닫고 롤백하지 않는다")
    void restoreFailureAfterCommitStillClosesTheConnection() throws Exception {
        final var fixture = fixture();
        final var failure = new SQLException("restore");
        doThrow(failure).when(fixture.connection()).setAutoCommit(true);

        final var thrown = assertThrows(TransactionCleanupException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown).hasMessage("Password change was committed, but connection cleanup failed");
        assertThat(thrown.getCause()).isSameAs(failure);
        verify(fixture.connection()).commit();
        verify(fixture.connection(), never()).rollback();
        verify(fixture.connection()).close();
    }

    @Test
    @DisplayName("커밋 후 복원과 종료가 모두 실패하면 정리 예외에 두 원인을 보존한다")
    void restoreAndCloseFailuresAfterCommitPreserveBothCauses() throws Exception {
        final var fixture = fixture();
        final var restoreFailure = new SQLException("restore");
        final var closeFailure = new SQLException("close");
        doThrow(restoreFailure).when(fixture.connection()).setAutoCommit(true);
        doThrow(closeFailure).when(fixture.connection()).close();

        final var thrown = assertThrows(TransactionCleanupException.class,
                () -> fixture.service().changePassword(1L, "newPassword", "operator"));

        assertThat(thrown.getCause()).isSameAs(restoreFailure);
        assertThat(restoreFailure.getSuppressed()).containsExactly(closeFailure);
        verify(fixture.connection()).commit();
        verify(fixture.connection(), never()).rollback();
        verify(fixture.connection()).close();
    }

    @Test
    @DisplayName("기존 자동 커밋 값이 false인 연결은 원래 값으로 복원한다")
    void restoresTheOriginalAutoCommitSetting() throws Exception {
        final var fixture = fixture();
        when(fixture.connection().getAutoCommit()).thenReturn(false);

        fixture.service().changePassword(1L, "newPassword", "operator");

        final var order = inOrder(fixture.connection());
        order.verify(fixture.connection()).getAutoCommit();
        order.verify(fixture.connection()).setAutoCommit(false);
        order.verify(fixture.connection()).commit();
        order.verify(fixture.connection()).setAutoCommit(false);
        order.verify(fixture.connection()).close();
        verify(fixture.connection(), never()).setAutoCommit(true);
    }

    @Test
    @DisplayName("기존 트랜잭션이 있으면 새로 시작하지 않고 기존 등록과 연결을 보존한다")
    void existingTransactionIsRejectedWithoutReleasingItsConnection() throws Exception {
        final var fixture = fixture();
        TransactionSynchronizationManager.bindResource(fixture.dataSource(), fixture.connection());
        try {
            assertThrows(IllegalStateException.class,
                    () -> fixture.service().changePassword(1L, "newPassword", "operator"));

            assertThat(TransactionSynchronizationManager.getResource(fixture.dataSource()))
                    .isSameAs(fixture.connection());
            verifyNoInteractions(fixture.dataSource(), fixture.connection(), fixture.delegate());
        } finally {
            TransactionSynchronizationManager.unbindResource(fixture.dataSource());
        }
    }

    private Fixture fixture() throws SQLException {
        final var dataSource = mock(DataSource.class);
        dataSources.add(dataSource);
        final var connection = mock(Connection.class);
        final var delegate = mock(UserService.class);
        final var user = new User(1L, "gugu", "password", "gugu@example.com");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "newPassword", "operator");
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            return null;
        }).when(connection).close();
        return new Fixture(dataSource, connection, delegate, user, new TxUserService(delegate, dataSource));
    }

    private record Fixture(DataSource dataSource, Connection connection, UserService delegate,
                           User user, UserService service) {}
}

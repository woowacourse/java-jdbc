package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService delegate = mock(UserService.class);
    private final TxUserService service = new TxUserService(delegate, dataSource);

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("업무가 성공하면 커밋하고 커넥션을 닫은 뒤 등록을 해제한다")
    void commitsAndUnbindsConnectionAfterSuccess() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(delegate).changePassword(1L, "new", "user");

        service.changePassword(1L, "new", "user");

        verify(connection).commit();
        verify(connection).setAutoCommit(false);
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("자동 커밋 복구가 실패해도 업무 예외를 유지하고 복구 예외를 보존한다")
    void preservesBusinessExceptionWhenRestoringAutoCommitFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        var businessFailure = new IllegalStateException("business failure");
        var resetFailure = new SQLException("reset failure");
        doThrow(businessFailure).when(delegate).changePassword(1L, "new", "user");
        doThrow(resetFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> service.changePassword(1L, "new", "user"))
                .isSameAs(businessFailure)
                .hasSuppressedException(resetFailure);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("롤백이 실패하면 롤백 예외를 보존하고 자동 커밋을 복구하지 않는다")
    void preservesRollbackFailureWithoutRestoringAnUncertainTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        var businessFailure = new IllegalStateException("business failure");
        var rollbackFailure = new SQLException("rollback failure");
        doThrow(businessFailure).when(delegate).changePassword(1L, "new", "user");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> service.changePassword(1L, "new", "user"))
                .isSameAs(businessFailure)
                .hasSuppressedException(rollbackFailure);

        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("트랜잭션 준비가 실패해도 커넥션을 닫고 등록을 해제한다")
    void closesAndUnbindsWhenTransactionSetupFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenThrow(new SQLException("setup failure"));

        assertThatThrownBy(() -> service.changePassword(1L, "new", "user"))
                .isInstanceOf(DataAccessException.class);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("이미 등록된 커넥션이 있으면 이를 닫거나 등록 해제하지 않는다")
    void doesNotTakeOwnershipOfAnAlreadyBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> service.changePassword(1L, "new", "user"))
                .isInstanceOf(IllegalStateException.class);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

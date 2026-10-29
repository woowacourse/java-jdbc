package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TxUserServiceTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final UserService appUserService = mock(UserService.class);
    private final UserService userService = new TxUserService(dataSource, appUserService);

    @BeforeEach
    void setUp() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 비즈니스_로직_실행_중에는_Connection을_공유하고_커밋_후_정리한다() throws SQLException {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            return null;
        }).when(appUserService).changePassword(1L, "newPassword", "gugu");

        userService.changePassword(1L, "newPassword", "gugu");

        final var order = inOrder(connection, appUserService);
        order.verify(connection).setAutoCommit(false);
        order.verify(appUserService).changePassword(1L, "newPassword", "gugu");
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 예외가_발생하면_롤백하고_원래_예외를_던진다() throws SQLException {
        final var cause = new DataAccessException("history failed");
        doThrow(cause).when(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu")).isSameAs(cause);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Error가_발생해도_롤백하고_Connection을_정리한다() throws SQLException {
        final var cause = new AssertionError("business failed");
        doThrow(cause).when(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu")).isSameAs(cause);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 커밋_실패도_롤백하고_원인을_보존한다() throws SQLException {
        final var cause = new SQLException("commit failed");
        doThrow(cause).when(connection).commit();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 롤백과_자원_해제가_실패해도_비즈니스_예외를_보존한다() throws SQLException {
        final var cause = new DataAccessException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(cause).when(appUserService).changePassword(1L, "newPassword", "gugu");
        doThrow(rollbackFailure).when(connection).rollback();
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu")).isSameAs(cause);

        assertThat(cause.getSuppressed()).hasSize(2);
        assertThat(cause.getSuppressed()[0]).isSameAs(rollbackFailure);
        assertThat(cause.getSuppressed()[1]).hasCause(closeFailure);
        // 롤백 실패 후 autoCommit을 켜면 남은 변경이 커밋될 수 있다.
        verify(connection, never()).setAutoCommit(true);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 트랜잭션_시작에_실패해도_Connection을_닫는다() throws SQLException {
        final var cause = new SQLException("begin failed");
        doThrow(cause).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(appUserService, never()).changePassword(1L, "newPassword", "gugu");
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 참여한_트랜잭션에서_Error가_발생하면_롤백_상태를_남기고_원인을_보존한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var cause = new AssertionError("inner failed");
        doThrow(cause).when(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", "gugu")).isSameAs(cause);

        assertThat(TransactionSynchronizationManager.isRollbackOnly(dataSource)).isTrue();
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    void 기존_트랜잭션에_참여하면_그_Connection을_커밋하거나_닫지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        userService.changePassword(1L, "newPassword", "gugu");

        verify(appUserService).changePassword(1L, "newPassword", "gugu");
        verifyNoInteractions(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

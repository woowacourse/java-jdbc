package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService userService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        userService = mock(UserService.class);
        when(dataSource.getConnection()).thenReturn(connection);
        txUserService = new TxUserService(dataSource, userService);
    }

    @Test
    void 위임한_작업이_트랜잭션_연결을_쓰도록_바인딩하고_커밋한_뒤_정리한다() throws SQLException {
        final AtomicReference<Connection> boundDuringWork = new AtomicReference<>();
        doAnswer(invocation -> {
            boundDuringWork.set(TransactionSynchronizationManager.getResource(dataSource));
            return null;
        }).when(userService).changePassword(42L, "newPassword", "admin");

        txUserService.changePassword(42L, "newPassword", "admin");

        assertThat(boundDuringWork.get()).isSameAs(connection);
        final var order = inOrder(connection, userService);
        order.verify(connection).setAutoCommit(false);
        order.verify(userService).changePassword(42L, "newPassword", "admin");
        order.verify(connection).commit();
        order.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 위임한_작업이_실패하면_롤백하고_같은_예외를_전달한다() throws SQLException {
        final var failure = new DataAccessException("history failed");
        doThrow(failure).when(userService).changePassword(42L, "newPassword", "admin");

        assertThatThrownBy(() -> txUserService.changePassword(42L, "newPassword", "admin")).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 커밋이_실패하면_롤백하고_원인_예외를_보존한다() throws SQLException {
        final var failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> txUserService.changePassword(42L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 롤백도_실패하면_원래_예외를_유지하고_롤백_실패를_추가한다() throws SQLException {
        final var failure = new DataAccessException("history failed");
        final var rollbackFailure = new SQLException("rollback failed");
        doThrow(failure).when(userService).changePassword(42L, "newPassword", "admin");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> txUserService.changePassword(42L, "newPassword", "admin")).isSameAs(failure);

        assertThat(failure.getSuppressed()).containsExactly(rollbackFailure);
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Error가_발생해도_롤백하고_연결을_정리한_뒤_그대로_전달한다() throws SQLException {
        final var failure = new AssertionError("history failed");
        doThrow(failure).when(userService).changePassword(42L, "newPassword", "admin");

        assertThatThrownBy(() -> txUserService.changePassword(42L, "newPassword", "admin")).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 트랜잭션을_시작하지_못하면_작업을_위임하지_않고_연결을_닫는다() throws SQLException {
        final var failure = new SQLException("autoCommit failed");
        doThrow(failure).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> txUserService.changePassword(42L, "newPassword", "admin"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(userService, never()).changePassword(42L, "newPassword", "admin");
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 커밋_후_연결을_닫다가_실패해도_변경_결과를_실패로_바꾸지_않는다() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        assertThatCode(() -> txUserService.changePassword(42L, "newPassword", "admin"))
                .doesNotThrowAnyException();

        verify(connection).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 조회와_저장은_트랜잭션을_시작하지_않고_그대로_위임한다() throws SQLException {
        final var user = new User(42L, "gugu", "password", "user@example.com");
        when(userService.findById(42L)).thenReturn(user);

        assertThat(txUserService.findById(42L)).isSameAs(user);
        txUserService.save(user);

        verify(userService).save(user);
        verify(dataSource, never()).getConnection();
    }
}

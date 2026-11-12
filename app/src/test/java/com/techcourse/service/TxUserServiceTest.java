package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TxUserServiceTest {

    private DataSource dataSource;
    private Connection connection;
    private UserService appUserService;
    private TxUserService txUserService;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        appUserService = mock(UserService.class);

        given(dataSource.getConnection()).willReturn(connection);
        txUserService = new TxUserService(appUserService, dataSource);
    }

    @Test
    void 비밀번호_변경이_성공하면_커밋한다() throws SQLException {
        txUserService.changePassword(1L, "qqqqq", "gugu");

        final InOrder inOrder = inOrder(connection, appUserService);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(appUserService).changePassword(1L, "qqqqq", "gugu");
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 트랜잭션_동안에만_Connection을_현재_스레드에_묶어_둔다() {
        final List<Connection> boundConnections = new ArrayList<>();
        willAnswer(invocation -> boundConnections.add(TransactionSynchronizationManager.getResource(dataSource)))
                .given(appUserService).changePassword(1L, "qqqqq", "gugu");

        txUserService.changePassword(1L, "qqqqq", "gugu");

        assertThat(boundConnections).containsExactly(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 비밀번호_변경_중에_예외가_나면_롤백하고_예외를_다시_던진다() throws SQLException {
        final var failure = new DataAccessException("log failed");
        willThrow(failure).given(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
        verify(connection, never()).commit();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Error가_나도_롤백하고_다시_던진다() throws SQLException {
        final var failure = new StackOverflowError();
        willThrow(failure).given(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void commit이_실패하면_롤백하고_DataAccessException으로_바꿔_던진다() throws SQLException {
        final var commitFailure = new SQLException("commit failed");
        willThrow(commitFailure).given(connection).commit();

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(commitFailure);

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
    }

    @Test
    void commit_뒤에_Connection을_닫다_실패해도_예외를_던지지_않는다() throws SQLException {
        willThrow(new SQLException("close failed")).given(connection).close();

        assertThatCode(() -> txUserService.changePassword(1L, "qqqqq", "gugu"))
                .doesNotThrowAnyException();

        final InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 롤백마저_실패하면_원래_예외에_롤백_예외를_붙여_던진다() throws SQLException {
        final var rollbackFailure = new SQLException("rollback failed");
        willThrow(rollbackFailure).given(connection).rollback();
        willThrow(new DataAccessException("log failed")).given(appUserService).changePassword(1L, "newPassword", "gugu");

        assertThatThrownBy(() -> txUserService.changePassword(1L, "newPassword", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .satisfies(e -> assertThat(e.getSuppressed()).containsExactly(rollbackFailure));
        verify(connection).close();
    }

    @Test
    void 조회와_저장은_트랜잭션_없이_그대로_위임한다() throws SQLException {
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        given(appUserService.findById(1L)).willReturn(user);

        assertThat(txUserService.findById(1L)).isSameAs(user);
        txUserService.save(user);

        verify(appUserService).save(user);
        verify(dataSource, never()).getConnection();
    }
}

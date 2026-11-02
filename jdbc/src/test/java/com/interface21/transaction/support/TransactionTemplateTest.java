package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private TransactionTemplate transactionTemplate;
    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Test
    void 작업에_연결을_전달하고_성공하면_커밋한_뒤_설정을_복원하고_닫는다() throws SQLException {
        final var actualConnection = new AtomicReference<Connection>();

        transactionTemplate.execute(actualConnection::set);

        assertThat(actualConnection.get()).isSameAs(connection);
        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).setAutoCommit(false);
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 기존_자동_커밋_설정이_꺼져있으면_종료_후에도_유지한다() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(false);

        transactionTemplate.execute(connection -> {});

        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).setAutoCommit(false);
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).setAutoCommit(false);
        transactionOrder.verify(connection).close();
        verify(connection, never()).setAutoCommit(true);
    }

    @Test
    void 작업이_실패하면_롤백하고_원래_예외를_던진다() throws SQLException {
        final var failure = new IllegalStateException("작업 실패");

        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {
            throw failure;
        })).isSameAs(failure);

        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).rollback();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    void 커밋이_실패하면_롤백하고_SQL_예외를_변환한다() throws SQLException {
        final var failure = new SQLException("커밋 실패");
        doThrow(failure).when(connection).commit();

        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).rollback();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
    }

    @Test
    void 롤백이_실패하면_원래_예외에_추가하고_자동_커밋을_복원하지_않는다() throws SQLException {
        final var failure = new IllegalStateException("작업 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {
            throw failure;
        }))
                .isSameAs(failure)
                .satisfies(actual -> assertThat(actual.getSuppressed()).containsExactly(rollbackFailure));

        verify(connection, never()).setAutoCommit(true);
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void 롤백_후_설정_복원이_실패해도_원래_예외를_보존한다() throws SQLException {
        final var failure = new IllegalStateException("작업 실패");
        final var restoreFailure = new SQLException("자동 커밋 복원 실패");
        doThrow(restoreFailure).when(connection).setAutoCommit(true);

        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {
            throw failure;
        }))
                .isSameAs(failure)
                .satisfies(actual -> assertThat(actual.getSuppressed()).containsExactly(restoreFailure));

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void 연결_획득이_실패하면_작업을_실행하지_않고_SQL_예외를_변환한다() throws SQLException {
        final var failure = new SQLException("연결 획득 실패");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> transactionTemplate.execute(connection -> {
            throw new AssertionError("연결 획득에 실패하면 작업을 실행할 수 없습니다.");
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(connection);
    }
}

package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Test
    void 성공하면_커밋하고_오토커밋을_복구한_뒤_커넥션을_닫는다() throws SQLException {
        transactionTemplate.execute(() -> {});

        final var order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).commit();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
        verify(connection, never()).rollback();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 작업이_예외를_던지면_롤백하고_그_예외를_다시_던진다() throws SQLException {
        final var failure = new IllegalStateException("fail");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            throw failure;
        })).isSameAs(failure);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 커밋이_실패해도_롤백하고_DataAccessException으로_변환한다() throws SQLException {
        doThrow(new SQLException("commit fail")).when(connection).commit();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void 롤백이_실패해도_원래_예외가_유지되고_실패는_suppressed에_붙는다() throws SQLException {
        final var failure = new IllegalStateException("fail");
        final var rollbackFailure = new SQLException("rollback fail");
        doThrow(rollbackFailure).when(connection).rollback();

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
            throw failure;
        })).isSameAs(failure).hasSuppressedException(rollbackFailure);
    }

    @Test
    void 작업_안에서는_같은_커넥션이_스레드에_묶여_있다() {
        transactionTemplate.execute(() ->
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection));
    }
}

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
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void 트랜잭션_종료_후_연결_바인딩이_남지_않는다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 작업에서_공유_연결을_사용하고_성공하면_커밋한_뒤_설정을_복원하고_닫는다() throws SQLException {
        transactionTemplate.execute(() ->
                assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection));
        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).setAutoCommit(false);
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    void 작업이_실패하면_롤백하고_원래_예외를_던진다() throws SQLException {
        final var failure = new IllegalStateException("작업 실패");

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {
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

        assertThatThrownBy(() -> transactionTemplate.execute(() -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        final var transactionOrder = inOrder(connection);
        transactionOrder.verify(connection).commit();
        transactionOrder.verify(connection).rollback();
        transactionOrder.verify(connection).setAutoCommit(true);
        transactionOrder.verify(connection).close();
    }
}

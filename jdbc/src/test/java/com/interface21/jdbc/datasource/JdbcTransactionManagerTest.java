package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.transaction.PlatformTransactionManager;
import com.interface21.transaction.TransactionException;
import com.interface21.transaction.support.TransactionHolder;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTransactionManagerTest {

    private PlatformTransactionManager transactionManager;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        this.transactionManager = new JdbcTransactionManager(dataSource);
        this.connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void 트랜잭션을_시작한다() throws SQLException {
        // given
        when(connection.getAutoCommit()).thenReturn(true);

        // when
        TransactionHolder transaction = transactionManager.startTransaction();

        // then
        assertAll(
                () -> verify(connection).setAutoCommit(false),
                () -> assertThat(transaction.getTransaction()).isEqualTo(connection)
        );
    }
    
    @Test
    void 트랜잭션_시작_시_예외가_발생하면_TrasactionException_예외로_던진다() throws SQLException {
        // given
        when(connection.getAutoCommit()).thenThrow(new SQLException());

        // when & then
        assertThatThrownBy(() -> transactionManager.startTransaction())
                .isExactlyInstanceOf(TransactionException.class)
                .hasMessage("Database access error occurred");
    }

    @Test
    void 트랜잭션을_커밋한다() {
        // given
        TransactionHolder transaction = transactionManager.startTransaction();

        // when
        transactionManager.commit(transaction);

        // then
        assertAll(
                () -> verify(connection).commit(),
                () -> verify(connection).close()
        );
    }

    @Test
    void 트랜잭션_커밋_시_예외가_발생하면_TrasactionException_예외로_던진다() throws SQLException {
        // given
        doThrow(new SQLException()).when(connection).commit();

        // when & then
        assertThatThrownBy(() -> transactionManager.commit(transactionManager.startTransaction()))
                .isExactlyInstanceOf(TransactionException.class)
                .hasMessage("JDBC commit failed");
    }

    @Test
    void 트랜잭션을_롤백한다() {
        // given
        TransactionHolder transaction = transactionManager.startTransaction();

        // when
        transactionManager.rollback(transaction);

        // then
        assertAll(
                () -> verify(connection).rollback(),
                () -> verify(connection).close()
        );
    }

    @Test
    void 트랜잭션_롤백_시_예외가_발생하면_TrasactionException_예외로_던진다() throws SQLException {
        // given
        doThrow(new SQLException()).when(connection).rollback();

        // when & then
        assertThatThrownBy(() -> transactionManager.rollback(transactionManager.startTransaction()))
                .isExactlyInstanceOf(TransactionException.class)
                .hasMessage("JDBC rollback failed");
    }
}

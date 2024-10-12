package com.techcourse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionExecutor transactionExecutor;
    private TransactionFunction transactionFunction;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.transactionExecutor = mock(TransactionExecutor.class);
        this.transactionFunction = mock(TransactionFunction.class);
        this.transactionManager = new TransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void transactionByTransactionExecutor() throws SQLException {
        // given & when
        transactionManager.transaction(transactionExecutor);

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void transactionByTransactionFunction() throws SQLException {
        // given & when
        transactionManager.transaction(transactionFunction);

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).close();
    }

    @Test
    void rollback() throws SQLException {
        // given & when
        transactionManager.transaction(new TransactionExecutor() {
            @Override
            public void execute(Connection connection) throws SQLException {
                throw new SQLException("롤백 테스트");
            }
        });

        // then
        verify(connection).rollback();
    }
}

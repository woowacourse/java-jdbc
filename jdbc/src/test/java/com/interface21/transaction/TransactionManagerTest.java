package com.interface21.transaction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private Statement statement;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(Statement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    @DisplayName("트랜잭션 성공: 성공적으로 커밋")
    void doBegin() throws SQLException {
        final TransactionManager transactionManager = TransactionManager.getInstance();
        transactionManager.beginTransaction(dataSource, connection -> {
            try {
                connection.createStatement().execute("INSERT INTO test_table VALUES (1, 'test')");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        verify(connection, times(1)).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("트랜잭션 내의 예외 발생: 롤백")
    void testRollbackOnException() throws SQLException {
        final TransactionManager transactionManager = TransactionManager.getInstance();
        doThrow(new RuntimeException("트랜잭션 내 에러 발생")).when(statement).execute(any());

        assertThrows(RuntimeException.class, () -> {
            transactionManager.beginTransaction(dataSource, conn -> {
                try {
                    connection.createStatement().execute("INSERT INTO test_table VALUES (1, 'test')");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        verify(connection, never()).commit();
        verify(connection).rollback();
    }
}

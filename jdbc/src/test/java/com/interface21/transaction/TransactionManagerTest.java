package com.interface21.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        transactionManager = new TransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("Function 을 이용한 트랜잭션을 성공적으로 수행한다.")
    void executeTransactionWithFunction() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(true);

        String result = transactionManager.executeTransactionWithResult(conn -> "Success");

        assertThat(result).isEqualTo("Success");
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("Function 을 이용한 트랜잭션 중 예외가 발생하면 롤백한다.")
    void executeTransactionWithFunctionRollback() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(true);

        assertThatThrownBy(() -> transactionManager.executeTransactionWithResult(conn -> {
            throw new RuntimeException("Test exception");
        })).isInstanceOf(DataAccessException.class);

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("Consumer를 이용한 트랜잭션을 성공적으로 수행한다.")
    void executeTransactionWithConsumer() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(true);

        transactionManager.executeTransactionWithoutResult(conn -> {
        });

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("Consumer 트랜잭션 중 예외가 발생하면 롤백한다.")
    void executeTransactionWithConsumerRollback() throws SQLException {
        when(connection.getAutoCommit()).thenReturn(true);

        assertThatThrownBy(() -> transactionManager.executeTransactionWithoutResult(conn -> {
            throw new RuntimeException("Test exception");
        })).isInstanceOf(DataAccessException.class);

        verify(connection).setAutoCommit(false);
        verify(connection).rollback();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("SQLException 발생 시 DataAccessException으로 변환한다.")
    void handleSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database connection failed"));

        assertThatThrownBy(() -> transactionManager.executeTransactionWithResult(conn -> "Test"))
                .isInstanceOf(DataAccessException.class);
    }
}

package com.interface21.jdbc.transaction;

import com.interface21.jdbc.exception.JdbcAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        transactionManager = new TransactionManager();
    }

    @DisplayName("트랜잭션을 시작한다.")
    @Test
    void testBeginTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        transactionManager.begin(dataSource);

        verify(connection).setAutoCommit(false);
    }

    @DisplayName("시작된 트랜잭션에 대해 begin()을 호출할 경우 예외가 발생한다.")
    @Test
    void transactionAlreadyStarted() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager.begin(dataSource);

        assertThatThrownBy(() -> transactionManager.begin(dataSource))
                .isInstanceOf(JdbcAccessException.class)
                .hasMessage("Transaction already started for this datasource");
    }

    @DisplayName("트랜잭션을 커밋한다.")
    @Test
    void commitTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager.begin(dataSource);

        transactionManager.commit(dataSource);

        verify(connection).commit();
    }

    @DisplayName("트랜잭션을 롤백한다.")
    @Test
    void rollbackTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        transactionManager.begin(dataSource);

        transactionManager.rollback(dataSource);

        verify(connection).rollback();
    }

    @DisplayName("커넥션이 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void connectionNotFound() {
        assertThatThrownBy(() -> transactionManager.getConnection(dataSource))
                .isInstanceOf(JdbcAccessException.class)
                .hasMessage("Connection not found for this datasource");
    }
}

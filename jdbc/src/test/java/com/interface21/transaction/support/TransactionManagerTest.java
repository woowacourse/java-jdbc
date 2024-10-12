package com.interface21.transaction.support;

import static org.junit.jupiter.api.Assertions.*;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        transactionManager = new TransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("startTransaction 성공")
    @Test
    void startTransaction() throws SQLException {
        // given & when
        Connection conn = transactionManager.startTransaction();

        // then
        verify(connection).setAutoCommit(false);
        assertNotNull(conn);
    }

    @DisplayName("startTransaction 실패")
    @Test
    void startTransaction_dataAccessException() throws SQLException {
        // give
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        // when & then
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionManager.startTransaction());
        assertEquals("Failed to start transaction", exception.getMessage());
    }

    @DisplayName("commit 성공")
    @Test
    void commit() throws SQLException {
        // given & when
        transactionManager.commit(connection);

        // then
        verify(connection).commit();
    }

    @DisplayName("commit 실패")
    @Test
    void commit_dataAccessException() throws SQLException {
        // given
        doThrow(new SQLException("Commit error")).when(connection).commit();

        // when & then
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionManager.commit(connection));
        assertEquals("Failed to commit transaction", exception.getMessage());
    }

    @DisplayName("rollback 성공")
    @Test
    void rollback() throws SQLException {
        // given & when
        transactionManager.rollback(connection);

        // then:
        verify(connection).rollback();
    }

    @DisplayName("rollback 실패")
    @Test
    void rollback_dataAccessException() throws SQLException {
        // given
        doThrow(new SQLException("Rollback error")).when(connection).rollback();

        // when & then
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> transactionManager.rollback(connection));
        assertEquals("Failed to rollback transaction", exception.getMessage());
    }
}


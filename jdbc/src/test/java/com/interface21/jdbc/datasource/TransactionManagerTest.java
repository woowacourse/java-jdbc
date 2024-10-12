package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private TransactionManager transactionManager;
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        ConnectionManager connectionManager = mock(ConnectionManager.class);
        mockConnection = mock(Connection.class);
        transactionManager = new TransactionManager(connectionManager);

        doReturn(mockConnection).when(connectionManager).getConnection();
    }

    @DisplayName("오류가 발생하지 않는 function의 경우 commit된다")
    @Test
    void transactionFunctionSuccess() throws SQLException {
        Function<Connection, String> validFunction = connection -> "success";

        String actual = transactionManager.transaction(validFunction);

        assertAll(
                () -> assertThat(actual).isEqualTo("success"),
                () -> verify(mockConnection, times(1)).commit(),
                () -> verify(mockConnection, never()).rollback()
        );
    }

    @DisplayName("오류가 발생하는 function의 경우 rollback된다")
    @Test
    void transactionConsumerFail() throws SQLException {
        Consumer<Connection> exceptionConsumer = (connection) -> {
            throw new RuntimeException("exception");
        };

        assertAll(
                () -> assertThatThrownBy(() -> transactionManager.transaction(exceptionConsumer))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("exception"),
                () -> verify(mockConnection, never()).commit(),
                () -> verify(mockConnection, times(1)).rollback()
        );
    }

    @DisplayName("오류가 발생하지 않는 consumer의 경우 commit된다")
    @Test
    void transactionConsumerSuccess() throws SQLException {
        Consumer<Connection> validConsumer = mock(Consumer.class);

        transactionManager.transaction(validConsumer);

        assertAll(
                () -> verify(mockConnection, times(1)).commit(),
                () -> verify(mockConnection, never()).rollback()
        );
    }

    @DisplayName("오류가 발생하는 function의 경우 rollback된다")
    @Test
    void transactionFunctionFail() throws SQLException {
        Function<Connection, Void> exceptionFunction = (connection) -> {
            throw new RuntimeException("exception");
        };

        assertAll(
                ()-> assertThatThrownBy(()->transactionManager.transaction(exceptionFunction))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("exception"),
                () -> verify(mockConnection, never()).commit(),
                () -> verify(mockConnection, times(1)).rollback()
        );
    }
}

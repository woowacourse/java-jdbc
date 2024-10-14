package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.exception.SqlExecutionException;
import com.interface21.jdbc.exception.TransactionExecutionException;

class TransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private Consumer<Connection> consumer;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.consumer = mock(Consumer.class);
        this.transactionManager = new TransactionManager(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
    }

    @Test
    @DisplayName("정상적으로 트랜잭션이 실행되고 commit을 한다.")
    void execute() throws SQLException {
        //given

        //when
        transactionManager.execute(consumer);

        //then
        verify(connection).setAutoCommit(false);
        verify(consumer).accept(connection);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }

    @Test
    @DisplayName("예외가 발생하면 rollback을 한다.")
    void rollback() throws SQLException {
        //given
        doThrow(new SqlExecutionException("예외 발생"))
                .when(consumer).accept(connection);

        //when
        assertThatThrownBy(() -> transactionManager.execute(consumer))
                .isInstanceOf(TransactionExecutionException.class);

        //then
        verify(consumer).accept(connection);
        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
        verify(connection).setAutoCommit(true);
        verify(connection).close();
    }
}

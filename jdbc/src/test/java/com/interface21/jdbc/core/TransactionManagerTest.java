package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.exception.SqlExecutionException;
import com.interface21.jdbc.exception.TransactionExecutionException;

class TransactionManagerTest {

    private Connection connection;
    private Runnable runnable;

    @BeforeEach
    void setUp() {
        this.connection = mock(Connection.class);
        this.runnable = mock(Runnable.class);
    }

    @Test
    @DisplayName("정상적으로 트랜잭션이 실행되고 commit을 한다.")
    void execute() throws SQLException {
        //given

        //when
        TransactionManager.execute(connection, runnable);

        //then
        verify(connection).setAutoCommit(false);
        verify(runnable).run();
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("예외가 발생하면 rollback을 한다.")
    void rollback() throws SQLException {
        //given
        doThrow(new SqlExecutionException("예외 발생"))
                .when(runnable).run();

        //when
        assertThatThrownBy(() -> TransactionManager.execute(connection, runnable))
                .isInstanceOf(TransactionExecutionException.class);

        //then
        verify(runnable).run();
        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
    }
}

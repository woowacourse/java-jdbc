package com.interface21.jdbc.manager;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionManagerTest {

    private Connection connection;
    private Runnable runnable;

    @BeforeEach
    void setUp() {
        this.connection = mock(Connection.class);
        this.runnable = mock(Runnable.class);
    }

    @Test
    @DisplayName("예외가 발생되지 않는 경우 정상적으로 작동 후 commit 한다.")
    void start_WhenSuccess() throws SQLException {
        TransactionManager.start(connection, runnable);

        verify(connection).setAutoCommit(false);
        verify(runnable).run();
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("예외가 발생되는 경우 commit 하지 않고 rollback 한다.")
    void start_WhenException() throws SQLException {
        doThrow(new DataAccessException("exception 발생"))
                .when(runnable).run();

        assertThrows(DataAccessException.class, () ->
                TransactionManager.start(connection, runnable)
        );

        verify(runnable).run();
        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
    }
}

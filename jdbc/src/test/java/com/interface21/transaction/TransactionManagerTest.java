package com.interface21.transaction;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.jdbc.exception.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionManagerTest {

    private Connection connection;
    private Runnable runnable;

    @BeforeEach
    void setUp() {
        this.connection = mock(Connection.class);
        this.runnable = mock(Runnable.class);
    }

    @Test
    @DisplayName("트랜잭션 중간에 예외가 발생하지 않으면 rollback 하지 않고 commit 한다.")
    void start_WhenSuccess() throws SQLException {
        // when
        TransactionManager.transaction(connection, runnable);

        // then
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(runnable).run();
    }

    @Test
    @DisplayName("트랜잭션 중간에 예외가 발생하면 commit 하지 않고 rollback 한다.")
    void start_WhenException() throws SQLException {
        // given
        doThrow(new CannotGetJdbcConnectionException("exception in transaction"))
                .when(runnable).run();

        // when
        assertThatThrownBy(() -> TransactionManager.transaction(connection, runnable))
                .isInstanceOf(CannotGetJdbcConnectionException.class);

        // then
        verify(connection, never()).commit();
        verify(connection, atLeastOnce()).rollback();
        verify(runnable).run();
    }
}

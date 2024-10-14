package com.interface21.transaction.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionServiceTest {

    @DisplayName("메서드가 정상적으로 실행 완료되면 트랜잭션이 커밋된다.")
    @Test
    void commit() throws SQLException {
        // given
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        Runnable doNothing = () -> {};
        TransactionService.executeWithTransaction(doNothing, dataSource);

        // then
        verify(connection, times(1)).commit();
    }

    @DisplayName("메서드 실행 도중 예외가 발생하면 트랜잭션이 롤백된다.")
    @Test
    void rollback() throws SQLException {
        // given
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        Runnable throwException = () -> {
            throw new RuntimeException("비상!!!");
        };

        // when & then
        assertThatThrownBy(() -> TransactionService.executeWithTransaction(throwException, dataSource))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessage("Failed to commit transaction.");
        verify(connection, times(1)).rollback();
    }
}

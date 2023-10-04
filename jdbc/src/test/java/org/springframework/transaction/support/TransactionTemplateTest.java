package org.springframework.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    @Test
    void executeWithTransaction() throws SQLException {
        // given
        TransactionCallback transactionCallback = () -> System.out.println("성공");

        // when
        transactionTemplate.executeWithTransaction(transactionCallback);

        // then
        verify(connection, times(1)).setAutoCommit(false);
        verify(connection, times(1)).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void executeWithTransaction_RuntimeExceptionThrown_RollbackAndSameExceptionThrown() throws SQLException {
        // given
        RuntimeException exception = new DataAccessException("실패");
        TransactionCallback transactionCallback = () -> {
            throw exception;
        };

        // when, then
        assertThatThrownBy(() -> transactionTemplate.executeWithTransaction(transactionCallback))
                .isInstanceOf(exception.getClass());
        verify(connection, times(1)).setAutoCommit(false);
        verify(connection, times(1)).rollback();
        verify(connection, never()).commit();
    }


}

package com.interface21.transaction.support;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionTemplateTest {

    private TransactionTemplate transactionTemplate;
    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        transactionTemplate = new TransactionTemplate(dataSource);
    }

    @DisplayName("트랜잭션이 정상적으로 동작하면 commit 된다.")
    @Test
    void execute() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        transactionTemplate.execute(connection -> null);

        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    @DisplayName("트랜잭션 중 예외가 발생하면 commit 되지 않고 rollback 한다.")
    @Test
    void rollback() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        assertThrows(
                DataAccessException.class,
                () -> transactionTemplate.execute(connection -> { throw new DataAccessException(); })

        );

        verify(connection).rollback();
        verify(connection, never()).commit();
    }
}

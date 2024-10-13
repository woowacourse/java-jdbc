package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TransactionManagerTest {
    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @InjectMocks
    TransactionManager transactionManager;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        when(dataSource.getConnection()).thenReturn(connection);
    }


    @Test
    void testTransactionRollbackWithConsumer() throws SQLException {
        assertThatThrownBy(() -> transactionManager.performTransaction(this::throwExceptionForConsumer));

        verify(connection).rollback();
    }

    @Test
    void testTransactionRollbackWithFunction() throws SQLException {
        assertThatThrownBy(() -> transactionManager.performTransaction(connection -> {
            return throwExceptionForFunction(connection);
        }));

        verify(connection).rollback();
    }

    private void throwExceptionForConsumer(Connection connection) {
        throw new RuntimeException();
    }

    private <T> T throwExceptionForFunction(Connection connection) {
        throw new RuntimeException();
    }

    @Test
    void testTransactionCommitWithConsumer() throws SQLException {
        transactionManager.performTransaction(conn -> {
        });

        verify(connection).commit();
    }

    @Test
    void testTransactionCommitWithFunction() throws SQLException {
        transactionManager.performTransaction(connection -> null);

        verify(connection).commit();
    }
}

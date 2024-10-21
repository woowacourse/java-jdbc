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
    void testTransactionRollbackWithRunnable() throws SQLException {
        assertThatThrownBy(() -> transactionManager.performTransaction(this::throwExceptionForRunnable));

        verify(connection).rollback();
    }

    @Test
    void testTransactionRollbackWithSupplier() throws SQLException {
        assertThatThrownBy(() -> transactionManager.performTransaction(() -> throwExceptionForSupplier()));

        verify(connection).rollback();
    }

    private void throwExceptionForRunnable() {
        throw new RuntimeException();
    }

    private <T> T throwExceptionForSupplier() {
        throw new RuntimeException();
    }

    @Test
    void testTransactionCommitWithRunnable() throws SQLException {
        transactionManager.performTransaction(() -> {
        });

        verify(connection).commit();
    }

    @Test
    void testTransactionCommitWithSupplier() throws SQLException {
        transactionManager.performTransaction(() -> null);

        verify(connection).commit();
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void getConnectionWithoutTransaction() throws SQLException {
        Connection actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource))
                .isNull();

        DataSourceUtils.releaseConnection(actual, dataSource);

        verify(connection).close();
    }

    @Test
    void reuseConnectionWithinTransaction() throws SQLException {
        TransactionSynchronizationManager.bindResource(
                dataSource, connection
        );

        Connection actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);

        DataSourceUtils.releaseConnection(actual, dataSource);

        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        verify(connection).close();
    }

    @Test
    void connectionIsIsolatedByThread() throws InterruptedException {
        TransactionSynchronizationManager.bindResource(
                dataSource, connection
        );

        AtomicReference<Connection> otherThreadConnection =
                new AtomicReference<>();

        Thread thread = new Thread(() -> {
            otherThreadConnection.set(
                    TransactionSynchronizationManager.getResource(dataSource)
            );
        });

        thread.start();
        thread.join();

        assertThat(otherThreadConnection.get()).isNull();
        assertThat(TransactionSynchronizationManager.getResource(dataSource))
                .isSameAs(connection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource))
                .isNull();
    }
}

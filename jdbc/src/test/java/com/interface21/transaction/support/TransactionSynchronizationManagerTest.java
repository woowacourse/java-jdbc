package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void getResource_WhenNothingBound_ThenReturnNull() {

        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actual).isNull();
    }

    @Test
    void getResource_WhenBound_ThenReturnBoundConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        assertThat(actual).isSameAs(connection);
    }

    @Test
    void unbindResource_ThenReturnConnectionAndRemoveIt() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void unbindResource_ThenNotCloseConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        verify(connection, never()).close();
    }

    @Test
    void unbindResource_WhenNothingBound_ThenReturnNull() {
        final Connection actual = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(actual).isNull();
    }

    @Test
    void bindResource_WhenMultipleDataSources_ThenKeepEachSeparately() {
        final DataSource otherDataSource = mock(DataSource.class);
        final Connection otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(otherDataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isNull();
    }
}

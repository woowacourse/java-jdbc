package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionSynchronizationManagerTest {
    @Test
    void keepsResourcesPerThreadAndDataSourceAndClosesOnlyUnboundConnections() throws Exception {
        var first = mock(DataSource.class);
        var second = mock(DataSource.class);
        var firstConnection = mock(Connection.class);
        var secondConnection = mock(Connection.class);
        when(first.getConnection()).thenReturn(firstConnection);
        when(second.getConnection()).thenReturn(secondConnection);
        TransactionSynchronizationManager.bindResource(first, firstConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try {
            assertThat(DataSourceUtils.getConnection(first)).isSameAs(firstConnection);
            DataSourceUtils.releaseConnection(firstConnection, first);
            verify(firstConnection, never()).close();
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(first, secondConnection))
                    .isInstanceOf(IllegalStateException.class);
            try (var executor = Executors.newSingleThreadExecutor()) {
                assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(first)).get()).isNull();
            }
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
            DataSourceUtils.releaseConnection(firstConnection, first);
            verify(firstConnection).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
    }
}

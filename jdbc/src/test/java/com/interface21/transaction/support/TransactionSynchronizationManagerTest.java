package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void resourcesAreBoundAndUnboundPerDataSource() {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resourcesAreIsolatedBetweenThreads() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var mainConnection = mock(Connection.class);
        final var workerConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, mainConnection);

        try (var executor = Executors.newSingleThreadExecutor()) {
            final var workerResult = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, workerConnection);
                final var actual = TransactionSynchronizationManager.getResource(dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
                return actual;
            });

            assertThat(workerResult.get()).isSameAs(workerConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(mainConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

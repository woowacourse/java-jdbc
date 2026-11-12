package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        if (TransactionSynchronizationManager.getResource(otherDataSource) != null) {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void bindsAndUnbindsConnection() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void keepsConnectionsForDifferentDataSources() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void rejectsDuplicateBindingWithoutReplacingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void rejectsUnbindingMissingConnection() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isolatesThreadsAndCleansUpBeforeWorkerIsReused() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var workerConnection = mock(Connection.class);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var bound = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, workerConnection);
                try {
                    return TransactionSynchronizationManager.getResource(dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(bound).isSameAs(workerConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            final var afterCleanup = executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .get(5, TimeUnit.SECONDS);
            assertThat(afterCleanup).isNull();
        }
    }
}

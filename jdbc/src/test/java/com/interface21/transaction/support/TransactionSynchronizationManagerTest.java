package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
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
    void duplicateBindingPreservesOriginalConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void connectionsAreSeparatedByDataSource() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void connectionsAreSeparatedByThread() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .get(5, TimeUnit.SECONDS)).isNull();
        }
    }
}

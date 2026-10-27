package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanup() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void bindsAndUnbindsConnection() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    void rejectsDuplicateBindingWithoutReplacingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(
                dataSource, mock(Connection.class))).isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void keepsOtherDataSourceBoundWhenOneIsRemoved() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);
            TransactionSynchronizationManager.unbindResource(dataSource);
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void isolatesConnectionsBetweenThreads() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                final var otherConnection = mock(Connection.class);
                try {
                    TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }
}

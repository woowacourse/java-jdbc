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
    void resourcesAreManagedIndependentlyForEachDataSource() {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var firstConnection = mock(Connection.class);
        final var secondConnection = mock(Connection.class);

        assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
        TransactionSynchronizationManager.bindResource(first, firstConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(second);
        }
    }

    @Test
    void duplicateBindingDoesNotReplaceExistingConnection() {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void resourcesAreIsolatedBetweenThreads() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                final var otherConnection = mock(Connection.class);
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get();
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get()).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void unbindingMissingResourceFailsClearly() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(mock(DataSource.class)))
                .isInstanceOf(IllegalStateException.class);
    }
}

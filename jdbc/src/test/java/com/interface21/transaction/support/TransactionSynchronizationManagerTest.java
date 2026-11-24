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
    void bindAndUnbind() {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);

        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void resourcesAreSeparatedByDataSource() {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var firstConnection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(first, firstConnection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(second)).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
        }
    }

    @Test
    void resourcesAreSeparatedByThread() throws Exception {
        final var dataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));
        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var resourceInOtherThread = executor.submit(
                    () -> TransactionSynchronizationManager.getResource(dataSource));

            assertThat(resourceInOtherThread.get()).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void bindingTwiceIsRejected() {
        final var dataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));
        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void unbindingWithoutBindingIsRejected() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(mock(DataSource.class)))
                .isInstanceOf(IllegalStateException.class);
    }
}

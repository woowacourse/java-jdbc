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
import static org.mockito.Mockito.verifyNoInteractions;

class TransactionSynchronizationManagerTest {

    private final DataSource source = mock(DataSource.class);
    private final DataSource otherSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        for (var key : new DataSource[]{source, otherSource}) {
            if (TransactionSynchronizationManager.getResource(key) != null) {
                TransactionSynchronizationManager.unbindResource(key);
            }
        }
    }

    @Test
    void bindsRetrievesAndRemovesWithoutClosingConnection() {
        assertThat(TransactionSynchronizationManager.getResource(source)).isNull();
        TransactionSynchronizationManager.bindResource(source, connection);
        assertThat(TransactionSynchronizationManager.getResource(source)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(source)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(source)).isNull();
        verifyNoInteractions(connection);
        TransactionSynchronizationManager.bindResource(source, connection);
        assertThat(TransactionSynchronizationManager.getResource(source)).isSameAs(connection);
    }

    @Test
    void removingOneDataSourcePreservesTheOther() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(source, connection);
        TransactionSynchronizationManager.bindResource(otherSource, otherConnection);
        TransactionSynchronizationManager.unbindResource(source);
        assertThat(TransactionSynchronizationManager.getResource(source)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherSource)).isSameAs(otherConnection);
    }

    @Test
    void duplicateBindingFailsWithoutReplacingOriginal() {
        TransactionSynchronizationManager.bindResource(source, connection);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(source, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(source)).isSameAs(connection);
    }

    @Test
    void removingMissingResourceFails() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(source))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sameDataSourceHasIndependentConnectionsPerThread() throws Exception {
        TransactionSynchronizationManager.bindResource(source, connection);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(source)).isNull();
                final var otherConnection = mock(Connection.class);
                TransactionSynchronizationManager.bindResource(source, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(source)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(source);
                }
            }).get(5, TimeUnit.SECONDS);
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(source))
                    .get(5, TimeUnit.SECONDS)).isNull();
        }
        assertThat(TransactionSynchronizationManager.getResource(source)).isSameAs(connection);
    }
}

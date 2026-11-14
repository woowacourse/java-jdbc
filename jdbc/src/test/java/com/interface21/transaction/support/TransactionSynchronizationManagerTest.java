package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void connectionIsVisibleOnlyToTheThreadThatBoundIt() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (var executor = Executors.newSingleThreadExecutor()) {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            final var otherThreadResource = executor.submit(
                    () -> TransactionSynchronizationManager.getResource(dataSource));
            assertThat(otherThreadResource.get(5, TimeUnit.SECONDS)).isNull();
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

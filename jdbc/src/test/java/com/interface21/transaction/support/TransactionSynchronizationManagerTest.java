package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void bindingIsScopedToTheCurrentThreadAndDataSource() throws Exception {
        final var firstSource = mock(DataSource.class);
        final var secondSource = mock(DataSource.class);
        final var firstConnection = mock(Connection.class);
        final var secondConnection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(firstSource, firstConnection);
        try {
            TransactionSynchronizationManager.bindResource(secondSource, secondConnection);
            assertThat(CompletableFuture.supplyAsync(() -> TransactionSynchronizationManager.getResource(firstSource))
                    .get(5, TimeUnit.SECONDS)).isNull();
            assertThat(TransactionSynchronizationManager.unbindResource(firstSource)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(firstSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(secondSource)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(firstSource);
            TransactionSynchronizationManager.unbindResource(secondSource);
        }
    }
}

package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void boundConnectionIsVisibleOnlyOnTheCurrentThread() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(CompletableFuture.supplyAsync(
                    () -> TransactionSynchronizationManager.getResource(dataSource)).get()).isNull();
        } finally {
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        }
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rollbackOnlyIsClearedWhenResourceIsUnbound() {
        final var dataSource = mock(DataSource.class);
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));
        TransactionSynchronizationManager.setRollbackOnly(dataSource);
        assertThat(TransactionSynchronizationManager.isRollbackOnly(dataSource)).isTrue();

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.isRollbackOnly(dataSource)).isFalse();
    }

    @Test
    void cannotMarkRollbackOnlyWithoutBoundResource() {
        final var dataSource = mock(DataSource.class);

        assertThatThrownBy(() -> TransactionSynchronizationManager.setRollbackOnly(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }
}

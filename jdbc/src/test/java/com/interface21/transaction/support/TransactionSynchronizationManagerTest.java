package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Connection otherConnection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        unbindIfBound(dataSource);
        unbindIfBound(otherDataSource);
    }

    @Test
    void returnsNullWhenNothingIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void returnsBoundConnectionUntilItIsUnbound() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void keepsConnectionsSeparatedByDataSource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void rejectsBindingSecondConnectionForSameDataSource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void rejectsUnbindingWhenNothingIsBound() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNullKeyOrConnection() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(null, connection))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, null))
                .isInstanceOf(NullPointerException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void doesNotShareBoundConnectionWithOtherThreads() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection seenByOtherThread = CompletableFuture
                .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                .join();

        assertThat(seenByOtherThread).isNull();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    private void unbindIfBound(final DataSource key) {
        if (TransactionSynchronizationManager.getResource(key) != null) {
            TransactionSynchronizationManager.unbindResource(key);
        }
    }
}
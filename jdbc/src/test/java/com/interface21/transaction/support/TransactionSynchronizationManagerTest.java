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
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void bindAndGetResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void getResourceReturnsNullWhenNotBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var actual = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void bindResourceThrowsWhenAlreadyBound() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resourceIsNotSharedBetweenThreads() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var actual = executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get();

            assertThat(actual).isNull();
        }
    }
}

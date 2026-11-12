package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    private DataSource dataSource;
    private DataSource otherDataSource;
    private Connection connection;
    private Connection otherConnection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        otherDataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        otherConnection = mock(Connection.class);
    }

    @AfterEach
    void cleanUp() {
        unbindIfPresent(dataSource);
        unbindIfPresent(otherDataSource);
    }

    @Test
    void returnsNullWhenNoConnectionIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isNull();
        verifyNoInteractions(dataSource, otherDataSource, connection, otherConnection);
    }

    @Test
    void bindsAndUnbindsConnectionWithoutManagingItsLifecycle() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verifyNoInteractions(connection);
    }

    @Test
    void keepsConnectionsForDifferentDataSourcesIndependent() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.unbindResource(otherDataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isNull();
    }

    @Test
    void rejectsDuplicateBindingWithoutReplacingExistingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        verifyNoInteractions(connection, otherConnection);
    }

    @Test
    void rejectsNullBindingsWithoutLosingExistingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(null, otherConnection))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(otherDataSource, null))
                .isInstanceOf(NullPointerException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isNull();
    }

    @Test
    void rejectsUnbindingMissingConnectionWithoutRemovingOtherBindings() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(otherDataSource))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void isolatesConnectionsBoundToTheSameDataSourceInDifferentThreads() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var workerConnection = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    return TransactionSynchronizationManager.getResource(dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(workerConnection).isSameAs(otherConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    @Test
    void doesNotLeakBindingsWhenTheSameWorkerThreadIsReused() throws Exception {
        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var firstThread = executor.submit(() -> {
                TransactionSynchronizationManager.bindResource(dataSource, connection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
                    return Thread.currentThread().threadId();
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            final var secondThread = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                    return Thread.currentThread().threadId();
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(secondThread).isEqualTo(firstThread);
        }
    }

    private void unbindIfPresent(final DataSource key) {
        if (TransactionSynchronizationManager.getResource(key) != null) {
            TransactionSynchronizationManager.unbindResource(key);
        }
    }
}

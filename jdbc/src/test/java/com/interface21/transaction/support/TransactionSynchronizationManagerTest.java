package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void clearResources() {
        unbindIfPresent(dataSource);
        unbindIfPresent(otherDataSource);
    }

    @Test
    void returnsNullWhenNoResourceIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void bindsAndUnbindsTheSameConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void keepsOtherDataSourcesBoundWhenOneIsRemoved() {
        Connection otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.unbindResource(otherDataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isNull();
    }

    @Test
    void rejectsDuplicateBindingWithoutReplacingTheOriginalConnection() {
        Connection replacement = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, replacement))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void rejectsNullDataSource() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(null, connection))
                .isInstanceOf(NullPointerException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rejectsNullConnectionWithoutAffectingAnotherResource() {
        TransactionSynchronizationManager.bindResource(otherDataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, null))
                .isInstanceOf(NullPointerException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(connection);
    }

    @Test
    void rejectsUnbindingAnAbsentResource() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void isolatesThreadsAndLeavesAReusedWorkerClean() throws Exception {
        Connection workerConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (var worker = Executors.newSingleThreadExecutor()) {
            Thread firstThread = worker.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, workerConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(workerConnection);
                    return Thread.currentThread();
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);

            Thread reusedThread = worker.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, workerConnection);
                assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(workerConnection);
                return Thread.currentThread();
            }).get(5, TimeUnit.SECONDS);

            assertThat(reusedThread).isSameAs(firstThread);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    private void unbindIfPresent(DataSource source) {
        if (TransactionSynchronizationManager.getResource(source) != null) {
            TransactionSynchronizationManager.unbindResource(source);
        }
    }
}

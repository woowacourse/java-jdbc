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

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Connection otherConnection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    void unbindsConnectionAndAllowsNextTransaction() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
    }

    @Test
    void keepsDifferentDataSourcesIndependent() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void rejectsDuplicateBindingWithoutReplacingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void isolatesThreadsAndClearsConnectionBeforeThreadReuse() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            final var nextConnection = executor.submit(
                    () -> TransactionSynchronizationManager.getResource(dataSource)).get(5, TimeUnit.SECONDS);
            assertThat(nextConnection).isNull();
        }
    }
}

package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void keepsConnectionsSeparateForEachThread() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final Connection otherConnection = mock(Connection.class);

        CompletableFuture.runAsync(() -> {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            try {
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }).get(5, TimeUnit.SECONDS);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

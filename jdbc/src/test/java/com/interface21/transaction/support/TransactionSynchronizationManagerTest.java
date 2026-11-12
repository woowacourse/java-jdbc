package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 데이터소스별로_연결을_보관하고_해제한다() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        try {
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void 같은_데이터소스라도_스레드별로_서로_다른_연결을_보관한다() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get();
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get()).isNull();
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

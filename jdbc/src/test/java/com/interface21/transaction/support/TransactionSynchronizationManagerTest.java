package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final JdbcDataSource dataSource = createDataSource("transaction_sync");
    private final JdbcDataSource otherDataSource = createDataSource("transaction_sync_other");

    @AfterEach
    void clearResources() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    void bindsAndUnbindsConnectionsByDataSource() throws SQLException {
        try (var connection = dataSource.getConnection();
             var otherConnection = otherDataSource.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        }
    }

    @Test
    void doesNotShareConnectionsBetweenThreads() throws Exception {
        try (var connection = dataSource.getConnection();
             var executor = Executors.newSingleThreadExecutor()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final var resourceFromOtherThread = executor
                    .submit(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .get(5, TimeUnit.SECONDS);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(resourceFromOtherThread).isNull();
        }
    }

    private JdbcDataSource createDataSource(final String name) {
        final var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:" + name);
        return source;
    }
}

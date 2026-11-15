package com.interface21.transaction.support;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private DataSource otherDataSource;
    private Connection connection;
    private Connection otherConnection;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = createDataSource("transaction-synchronization");
        otherDataSource = createDataSource("other-transaction-synchronization");
        connection = dataSource.getConnection();
        otherConnection = otherDataSource.getConnection();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        if (TransactionSynchronizationManager.getResource(otherDataSource) != null) {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
        connection.close();
        otherConnection.close();
    }

    @Test
    void bindAndUnbind() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void keepConnectionsForDifferentDataSources() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void rejectDuplicateBinding() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void isolateConnectionsBetweenThreads() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var workerResource = executor.submit(() ->
                    TransactionSynchronizationManager.getResource(dataSource)
            ).get(5, TimeUnit.SECONDS);

            assertThat(workerResource).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }
    }

    private DataSource createDataSource(final String name) {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:" + name);
        return jdbcDataSource;
    }
}

package com.interface21.transaction.support;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionSynchronizationManagerTest {

    private JdbcConnectionPool dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "", "");
        connection = dataSource.getConnection();
    }

    @AfterEach
    void tearDown() throws SQLException {
        TransactionSynchronizationManager.unbindResource(dataSource);
        connection.close();
        dataSource.dispose();
    }

    @Test
    void returnsNullWhenNothingIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    void bindsAndUnbindsConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void rejectsBindingTwiceForTheSameDataSource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void keepsConnectionsPerDataSource() throws SQLException {
        final var other = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "", "");
        try (final var otherConnection = other.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.bindResource(other, otherConnection);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(other)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(other);
            other.dispose();
        }
    }

    @Test
    void isolatesBindingsBetweenThreads() throws InterruptedException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var seenByOtherThread = new AtomicReference<Connection>(connection);

        final var thread = new Thread(() ->
                seenByOtherThread.set(TransactionSynchronizationManager.getResource(dataSource)));
        thread.start();
        thread.join();

        assertThat(seenByOtherThread.get()).isNull();
    }
}

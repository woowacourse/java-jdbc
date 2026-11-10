package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;
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
    void 바인딩_전에는_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 바인딩한_커넥션을_조회한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 언바인딩하면_커넥션을_반환하고_이후_조회는_null이다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(
            connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 다른_스레드에서는_보이지_않는다() throws InterruptedException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        AtomicReference<Connection> seen = new AtomicReference<>();

        Thread other = new Thread(
            () -> seen.set(TransactionSynchronizationManager.getResource(dataSource)));
        other.start();
        other.join();

        assertThat(seen.get()).isNull();
    }
}
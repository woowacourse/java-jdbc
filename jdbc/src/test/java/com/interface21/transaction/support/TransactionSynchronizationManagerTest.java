package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
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
    void 바인딩한_커넥션을_DataSource로_조회한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 바인딩을_해제하면_해제한_커넥션을_반환하고_더_이상_조회되지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 다른_스레드에서는_바인딩한_커넥션이_보이지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var fromOtherThread = CompletableFuture
                .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                .join();

        assertThat(fromOtherThread).isNull();
    }
}

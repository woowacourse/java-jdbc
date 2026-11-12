package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 바인딩한_연결을_같은_스레드에서_조회한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 다른_스레드에서는_바인딩한_연결을_조회하지_못한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection fromOtherThread = CompletableFuture
                .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                .join();

        assertThat(fromOtherThread).isNull();
    }

    @Test
    void 데이터소스마다_연결을_따로_보관한다() {
        final DataSource otherDataSource = mock(DataSource.class);
        final Connection otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    void 해제하면_바인딩했던_연결을_반환하고_더_이상_조회되지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 이미_연결이_바인딩된_데이터소스에_다시_바인딩하면_IllegalStateException을_던진다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 바인딩되지_않은_데이터소스를_해제하면_IllegalStateException을_던진다() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }
}

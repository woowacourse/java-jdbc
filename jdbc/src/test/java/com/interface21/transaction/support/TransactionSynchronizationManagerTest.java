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
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 바인딩한_DataSource로_조회하면_바인딩한_Connection을_반환한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 바인딩하지_않은_DataSource로_조회하면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void DataSource마다_다른_Connection을_보관한다() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);

        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    void 이미_바인딩된_DataSource에_다시_바인딩하면_예외가_발생한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 바인딩을_해제하면_해제한_Connection을_반환하고_더_이상_조회되지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 바인딩하지_않은_DataSource를_해제하면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    void 다른_스레드에서는_바인딩한_Connection을_조회할_수_없다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final var connectionInOtherThread = CompletableFuture
                .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                .join();

        assertThat(connectionInOtherThread).isNull();
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    @Test
    void boundConnectionIsOnlyReusedByItsThreadAndDataSource() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var otherDataSource = mock(DataSource.class);
        final var transactionConnection = mock(Connection.class);
        final var standaloneConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(standaloneConnection);
        when(otherDataSource.getConnection()).thenReturn(standaloneConnection);

        TransactionSynchronizationManager.bindResource(dataSource, transactionConnection);
        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(transactionConnection);
            assertThat(DataSourceUtils.getConnection(otherDataSource)).isSameAs(standaloneConnection);
            assertThat(CompletableFuture.supplyAsync(() -> DataSourceUtils.getConnection(dataSource))
                    .get(3, TimeUnit.SECONDS)).isSameAs(standaloneConnection);

            DataSourceUtils.releaseConnection(transactionConnection, dataSource);
            verify(transactionConnection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }

        DataSourceUtils.releaseConnection(transactionConnection, dataSource);
        verify(transactionConnection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

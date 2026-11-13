package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void unbindOnlyRemovesTheConnectionForTheGivenDataSource() {
        var firstDataSource = mock(DataSource.class);
        var secondDataSource = mock(DataSource.class);
        var firstConnection = mock(Connection.class);
        var secondConnection = mock(Connection.class);

        assertThat(TransactionSynchronizationManager.getResource(firstDataSource)).isNull();
        TransactionSynchronizationManager.bindResource(firstDataSource, firstConnection);
        TransactionSynchronizationManager.bindResource(secondDataSource, secondConnection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(firstDataSource)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.unbindResource(firstDataSource)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(firstDataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(secondDataSource)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(firstDataSource);
            TransactionSynchronizationManager.unbindResource(secondDataSource);
        }
        assertThat(TransactionSynchronizationManager.getResource(secondDataSource)).isNull();
    }

    @Test
    void connectionBoundInOneThreadIsNotVisibleInAnotherThread() throws Exception {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            var lookup = new FutureTask<>(() -> TransactionSynchronizationManager.getResource(dataSource));
            Thread.startVirtualThread(lookup);

            assertThat(lookup.get(5, TimeUnit.SECONDS)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

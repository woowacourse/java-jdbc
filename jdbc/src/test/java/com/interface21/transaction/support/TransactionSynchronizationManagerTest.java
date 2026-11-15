package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionSynchronizationManagerTest {

    @Test
    void boundConnectionIsReusedAndReleasedAfterUnbinding() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
            verify(connection, never()).close();
            verify(dataSource, never()).getConnection();
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void connectionOutsideTransactionIsNotBound() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void resourcesAreIsolatedByThreadAndDataSource() throws Exception {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var mainConnection = mock(Connection.class);
        final var secondConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(first, mainConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
                final var workerConnection = mock(Connection.class);
                TransactionSynchronizationManager.bindResource(first, workerConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(workerConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(first);
                }
            }).get();
            executor.submit(() -> assertThat(TransactionSynchronizationManager.getResource(first)).isNull()).get();
            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(mainConnection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(first))
                .isInstanceOf(IllegalStateException.class);
    }
}

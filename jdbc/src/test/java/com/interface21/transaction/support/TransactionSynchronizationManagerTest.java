package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void boundConnectionIsReusedAndNotClosedByRelease() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void unboundConnectionIsClosedAfterUse() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(actual, dataSource);

        verify(connection).close();
    }

    @Test
    void resourcesAreIsolatedByDataSourceAndThread() throws Exception {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get())
                    .isNull();
        }
        assertThat(TransactionSynchronizationManager.unbindResource(otherDataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void bindingSameDataSourceTwiceDoesNotReplaceConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

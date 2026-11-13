package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    @Test
    void getAndReleaseConnection_WithoutBinding_ClosesStandaloneConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        Connection actual = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void getAndReleaseConnection_WithBinding_ReusesConnectionUntilUnbound() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            Connection actual = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(actual, dataSource);

            assertThat(actual).isSameAs(connection);
            verifyNoInteractions(dataSource);
            verify(connection, never()).close();
        } finally {
            DataSourceUtils.releaseConnection(TransactionSynchronizationManager.unbindResource(dataSource), dataSource);
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void getConnection_WithBindingOnAnotherThread_UsesDifferentConnection() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection boundConnection = mock(Connection.class);
        Connection standaloneConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(standaloneConnection);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        try (var executor = Executors.newSingleThreadExecutor()) {
            var otherThreadConnection = executor.submit(() -> {
                Connection connection = DataSourceUtils.getConnection(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
                return connection;
            });

            assertThat(otherThreadConnection.get(5, TimeUnit.SECONDS)).isSameAs(standaloneConnection);
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(boundConnection);
            verify(standaloneConnection).close();
            verify(boundConnection, never()).close();
        } finally {
            DataSourceUtils.releaseConnection(TransactionSynchronizationManager.unbindResource(dataSource), dataSource);
        }
    }
}

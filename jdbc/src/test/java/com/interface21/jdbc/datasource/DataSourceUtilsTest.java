package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    @Test
    void closesConnectionsOpenedOutsideATransaction() throws SQLException {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        var acquired = DataSourceUtils.getConnection(dataSource);
        try {
            assertThat(acquired).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            DataSourceUtils.releaseConnection(acquired, dataSource);
            verify(connection).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void reusesBoundConnectionAndClosesItOnlyAfterUnbinding() throws SQLException {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            var acquired = DataSourceUtils.getConnection(dataSource);

            assertThat(acquired).isSameAs(connection);
            verifyNoInteractions(dataSource);
            DataSourceUtils.releaseConnection(acquired, dataSource);
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        verify(connection).close();
    }
}

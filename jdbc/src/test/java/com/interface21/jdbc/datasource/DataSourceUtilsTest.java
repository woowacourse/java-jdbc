package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanup() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void obtainsOrdinaryConnectionWithoutBindingIt() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void reusesBoundConnectionAndClosesItOnlyAfterUnbinding() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verify(dataSource, never()).getConnection();
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection, never()).close();
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void closesUnboundConnectionEvenWhenAnotherConnectionIsBound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void preservesAcquisitionFailure() throws SQLException {
        final var failure = new SQLException("Connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);
        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(failure);
    }

    @Test
    void preservesCloseFailure() throws SQLException {
        final var failure = new SQLException("Close failed");
        doThrow(failure).when(connection).close();
        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(failure);
    }
}

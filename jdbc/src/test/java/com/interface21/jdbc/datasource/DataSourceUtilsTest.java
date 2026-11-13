package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private DataSource otherDataSource;
    private Connection connection;
    private Connection otherConnection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        otherDataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        otherConnection = mock(Connection.class);
    }

    @AfterEach
    void cleanUp() {
        unbindIfPresent(dataSource);
        unbindIfPresent(otherDataSource);
    }

    @Test
    void standaloneConnectionsAreNotBoundAndCanBeReleasedAfterEachOperation() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection, otherConnection);

        final var first = DataSourceUtils.getConnection(dataSource);
        assertThat(first).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(first, dataSource);

        final var second = DataSourceUtils.getConnection(dataSource);
        assertThat(second).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(second, dataSource);

        verify(dataSource, times(2)).getConnection();
        verify(connection).close();
        verify(otherConnection).close();
    }

    @Test
    void reusesAndPreservesBoundConnectionUntilItIsUnbound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource, connection);

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void releasesDifferentConnectionWithoutTouchingTheBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verifyNoInteractions(connection, dataSource);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void onlyPreservesConnectionBoundToTheRequestedDataSource() throws SQLException {
        TransactionSynchronizationManager.bindResource(otherDataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource, otherDataSource);
    }

    @Test
    void ignoresNullConnectionOnRelease() {
        DataSourceUtils.releaseConnection(null, dataSource);

        verifyNoInteractions(dataSource);
    }

    @Test
    void preservesSQLExceptionWhenConnectionCannotBeObtained() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesSQLExceptionWhenConnectionCannotBeClosed() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
    }

    private void unbindIfPresent(final DataSource key) {
        if (TransactionSynchronizationManager.getResource(key) != null) {
            TransactionSynchronizationManager.unbindResource(key);
        }
    }
}

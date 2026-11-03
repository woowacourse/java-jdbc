package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
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

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @Test
    void normalConnectionIsNotBoundAndIsClosedOnRelease() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void boundConnectionIsReusedAndClosedOnlyAfterUnbinding() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
            verify(dataSource, never()).getConnection();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void unrelatedConnectionIsClosedWithoutReleasingBoundConnection() throws SQLException {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            DataSourceUtils.releaseConnection(otherConnection, dataSource);

            verify(otherConnection).close();
            verify(connection, never()).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void acquisitionFailureKeepsOriginalCauseAndDoesNotBindResource() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(cause);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void closeFailureKeepsOriginalCause() throws SQLException {
        final var cause = new SQLException("close failed");
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(cause);
    }
}

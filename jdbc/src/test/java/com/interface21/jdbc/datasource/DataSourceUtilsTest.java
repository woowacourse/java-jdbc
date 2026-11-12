package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void returnsBoundConnectionWithoutAcquiringAnotherOne() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void ordinaryConnectionsAreNotBoundToThread() throws SQLException {
        final var secondConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection, secondConnection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(secondConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void keepsBoundConnectionOpenUntilUnbound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void closesOrdinaryConnectionEvenWhenAnotherConnectionIsBound() throws SQLException {
        final var boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        verify(boundConnection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(boundConnection);
    }

    @Test
    void acquisitionFailurePreservesCauseAsDataAccessException() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void closeFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void closeFailureIsSuppressedOnOriginalFailure() throws SQLException {
        final var failure = new IllegalStateException("operation failed");
        final var closeFailure = new SQLException("close failed");
        doThrow(closeFailure).when(connection).close();

        DataSourceUtils.releaseConnection(connection, dataSource, failure);

        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
        verify(connection).close();
    }
}

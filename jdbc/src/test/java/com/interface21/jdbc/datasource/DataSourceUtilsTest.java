package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void obtainsAndClosesConnectionOutsideTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        DataSourceUtils.releaseConnection(actual, dataSource);
        verify(connection).close();
    }

    @Test
    void reusesBoundConnectionAndClosesItOnlyAfterUnbinding() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verifyNoInteractions(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void releasesUnboundConnectionWithoutTouchingTransactionConnection() throws SQLException {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verifyNoInteractions(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void preservesCauseWhenObtainingConnectionFails() throws SQLException {
        final var failure = new SQLException("Connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesCauseWhenClosingConnectionFails() throws SQLException {
        final var failure = new SQLException("Close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }
}

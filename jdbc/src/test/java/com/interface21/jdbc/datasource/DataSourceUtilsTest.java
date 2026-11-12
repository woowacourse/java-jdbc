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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void reusesBoundConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource);
    }

    @Test
    void ordinaryConnectionIsNotBoundAndIsClosedAfterUse() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        final var actual = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void boundConnectionStaysOpenUntilUnbound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);

        final var actual = TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        verify(connection).close();
    }

    @Test
    void closesConnectionThatDoesNotMatchBoundConnection() throws SQLException {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verifyNoInteractions(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void ignoresNullConnection() {
        DataSourceUtils.releaseConnection(null, dataSource);

        verifyNoInteractions(dataSource);
    }

    @Test
    void preservesConnectionAcquisitionFailure() throws SQLException {
        final var cause = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesConnectionReleaseFailure() throws SQLException {
        final var cause = new SQLException("반납 실패");
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
    }
}

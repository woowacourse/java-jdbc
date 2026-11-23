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
    void returnsBoundConnectionWithoutOpeningNewOne() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource);
    }

    @Test
    void opensNewConnectionWithoutBindingItOutsideTransaction() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void convertsAcquisitionFailureAndPreservesCause() throws SQLException {
        final SQLException failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void closesConnectionThatIsNotBoundToTransaction() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void keepsConnectionThatIsBoundToTransactionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void closesOtherConnectionEvenWhileTransactionIsActive() throws SQLException {
        final Connection otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verify(connection, never()).close();
    }

    @Test
    void convertsCloseFailureAndPreservesCause() throws SQLException {
        final SQLException failure = new SQLException("cannot close");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void attachesCloseFailureToPrimaryFailureInsteadOfThrowing() throws SQLException {
        final RuntimeException primary = new RuntimeException("operation failed");
        final SQLException closeFailure = new SQLException("cannot close");
        doThrow(closeFailure).when(connection).close();

        DataSourceUtils.releaseConnection(connection, dataSource, primary);

        assertThat(primary.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    void attachesRuntimeCloseFailureToPrimaryFailure() throws SQLException {
        final RuntimeException primary = new RuntimeException("operation failed");
        final IllegalStateException closeFailure = new IllegalStateException("driver bug on close");
        doThrow(closeFailure).when(connection).close();

        DataSourceUtils.releaseConnection(connection, dataSource, primary);

        assertThat(primary.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    void throwsCloseFailureWhenThereIsNoPrimaryFailure() throws SQLException {
        final SQLException closeFailure = new SQLException("cannot close");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource, null))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
    }

    @Test
    void keepsBoundConnectionOpenEvenWhenOperationFailed() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource, new RuntimeException("operation failed"));

        verify(connection, never()).close();
    }
}
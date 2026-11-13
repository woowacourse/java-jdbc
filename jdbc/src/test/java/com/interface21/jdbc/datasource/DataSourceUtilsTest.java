package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void clearResource() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void returnsTheBoundConnectionWithoutAcquiringAnother() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);

        verify(dataSource, never()).getConnection();
    }

    @Test
    void acquiresStandaloneConnectionsWithoutBindingThem() throws SQLException {
        Connection nextConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection, nextConnection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(nextConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void preservesAcquisitionFailureAndLeavesNoBinding() throws SQLException {
        SQLException failure = new SQLException("cannot acquire");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void ignoresNullConnections() {
        assertThatCode(() -> DataSourceUtils.releaseConnection(null, dataSource)).doesNotThrowAnyException();
    }

    @Test
    void retainsTheConnectionOwnedByTheTransaction() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void closesAnUnboundConnection() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void closesADifferentConnectionWithoutReleasingTheBoundOne() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        verify(boundConnection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(boundConnection);
    }

    @Test
    void closesTheTransactionConnectionAfterItHasBeenUnbound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.unbindResource(dataSource);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void preservesTheCauseOfACloseFailure() throws SQLException {
        SQLException failure = new SQLException("cannot close");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void suppressesACloseFailureWithoutReplacingThePrimaryFailure() throws SQLException {
        SQLException closeFailure = new SQLException("cannot close");
        IllegalStateException primaryFailure = new IllegalStateException("query failed");
        doThrow(closeFailure).when(connection).close();

        assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource, primaryFailure))
                .doesNotThrowAnyException();

        assertThat(primaryFailure.getSuppressed()).hasSize(1);
        assertThat(primaryFailure.getSuppressed()[0])
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
    }

    @Test
    void throwsACloseFailureWhenThereIsNoPrimaryFailure() throws SQLException {
        SQLException closeFailure = new SQLException("cannot close");
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource, null))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeFailure);
    }

    @Test
    void doesNotAddSuppressedFailuresWhenReleaseSucceeds() throws SQLException {
        IllegalStateException primaryFailure = new IllegalStateException("query failed");

        DataSourceUtils.releaseConnection(connection, dataSource, primaryFailure);

        verify(connection).close();
        assertThat(primaryFailure.getSuppressed()).isEmpty();
    }

    @Test
    void suppressesUncheckedCloseFailuresWithoutReplacingThePrimaryFailure() throws SQLException {
        IllegalStateException closeFailure = new IllegalStateException("cannot close");
        IllegalStateException primaryFailure = new IllegalStateException("query failed");
        doThrow(closeFailure).when(connection).close();

        assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource, primaryFailure))
                .doesNotThrowAnyException();

        assertThat(primaryFailure.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    void suppressesCloseErrorsWithoutReplacingThePrimaryFailure() throws SQLException {
        AssertionError closeFailure = new AssertionError("cannot close");
        IllegalStateException primaryFailure = new IllegalStateException("query failed");
        doThrow(closeFailure).when(connection).close();

        assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource, primaryFailure))
                .doesNotThrowAnyException();

        assertThat(primaryFailure.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    void keepsTheTransactionConnectionOpenEvenWhenThereIsAPrimaryFailure() throws SQLException {
        IllegalStateException primaryFailure = new IllegalStateException("query failed");
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource, primaryFailure);

        verify(connection, never()).close();
        assertThat(primaryFailure.getSuppressed()).isEmpty();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

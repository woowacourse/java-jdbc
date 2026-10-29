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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
    void 트랜잭션에_등록된_Connection을_재사용한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource);
    }

    @Test
    void 트랜잭션_밖에서는_Connection을_등록하지_않고_사용_후_닫는다() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);

        final var actual = DataSourceUtils.getConnection(dataSource);
        DataSourceUtils.releaseConnection(actual, dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void 트랜잭션에_등록된_Connection은_해제하기_전까지_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection, never()).close();

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void 같은_DataSource의_다른_Connection은_닫는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verify(connection, never()).close();
    }

    @Test
    void Connection을_얻지_못하면_원인을_보존한다() throws SQLException {
        final var cause = new SQLException("connection unavailable");
        given(dataSource.getConnection()).willThrow(cause);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Connection을_닫지_못하면_원인을_보존한다() throws SQLException {
        final var cause = new SQLException("close failed");
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(cause);
    }
}

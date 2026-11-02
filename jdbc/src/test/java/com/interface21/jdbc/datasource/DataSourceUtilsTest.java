package com.interface21.jdbc.datasource;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 바인딩된_Connection이_있으면_새로_얻지_않고_바인딩된_Connection을_반환한다() throws SQLException {
        final var boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(boundConnection);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void 바인딩된_Connection이_없으면_새로_얻고_바인딩하지_않는다() {
        final var actual = DataSourceUtils.getConnection(dataSource);

        assertThat(actual).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Connection을_얻지_못하면_CannotGetJdbcConnectionException이_발생한다() throws SQLException {
        final var sqlException = new SQLException("connection refused");
        when(dataSource.getConnection()).thenThrow(sqlException);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(sqlException);
    }

    @Test
    void 바인딩된_Connection은_반납해도_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
    }

    @Test
    void 바인딩되지_않은_Connection은_반납하면_닫는다() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void Connection을_닫지_못하면_CannotGetJdbcConnectionException이_발생한다() throws SQLException {
        final var sqlException = new SQLException("close failed");
        doThrow(sqlException).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(sqlException);
    }
}

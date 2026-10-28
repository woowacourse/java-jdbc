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
import static org.mockito.Mockito.times;
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

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void 등록된_커넥션이_없으면_호출마다_새_커넥션을_가져온다() throws SQLException {
        final var otherConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection, otherConnection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(dataSource, times(2)).getConnection();
    }

    @Test
    void 등록된_커넥션이_있으면_새로_가져오지_않고_재사용한다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void 커넥션을_가져오지_못하면_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(exception);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(exception);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 등록된_커넥션은_반환해도_닫거나_등록을_해제하지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 등록되지_않은_커넥션은_반환하면_닫는다() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void 등록된_커넥션과_다른_커넥션을_반환하면_전달받은_커넥션만_닫는다() throws SQLException {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(otherConnection, dataSource);

        verify(otherConnection).close();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 등록을_해제한_커넥션은_반환하면_닫는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.unbindResource(dataSource);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 커넥션이_null이면_등록된_커넥션에_영향을_주지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(null, dataSource);

        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void 커넥션을_닫지_못하면_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("연결 종료 실패");
        doThrow(exception).when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(exception);
    }
}

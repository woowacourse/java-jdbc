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
import static org.assertj.core.api.Assertions.assertThatCode;
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

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 트랜잭션에_바인딩된_연결이_있으면_새_연결을_얻지_않고_그_연결을_반환한다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        verify(dataSource, never()).getConnection();
    }

    @Test
    void 바인딩된_연결이_없으면_새_연결을_얻되_스레드에_보관하지_않는다() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 연결을_얻지_못하면_원인을_담아_CannotGetJdbcConnectionException을_던진다() throws SQLException {
        final var failure = new SQLException("connection refused");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void 트랜잭션에_바인딩된_연결은_반환해도_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
    }

    @Test
    void 바인딩되지_않은_연결은_반환하면_닫는다() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void 연결을_닫다가_실패해도_예외를_던지지_않는다() throws SQLException {
        doThrow(new SQLException("close failed")).when(connection).close();

        assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .doesNotThrowAnyException();
    }
}

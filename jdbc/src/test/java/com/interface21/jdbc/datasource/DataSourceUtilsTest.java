package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void 스레드에_묶인_Connection이_있으면_새로_얻지_않고_그_Connection을_돌려준다() throws SQLException {
        final var boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);

        try {
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(boundConnection);
            verify(dataSource, never()).getConnection();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 스레드에_묶인_Connection이_없으면_새로_얻고_스레드에_묶지_않는다() {
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void Connection을_얻지_못하면_CannotGetJdbcConnectionException을_던진다() throws SQLException {
        final var cause = new SQLException("connection refused");
        given(dataSource.getConnection()).willThrow(cause);

        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void 스레드에_묶인_Connection은_release해도_닫지_않는다() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 스레드에_묶이지_않은_Connection은_release하면_닫는다() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    void Connection을_닫다가_실패해도_예외를_던지지_않는다() throws SQLException {
        willThrow(new SQLException("close failed")).given(connection).close();

        assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .doesNotThrowAnyException();
    }
}

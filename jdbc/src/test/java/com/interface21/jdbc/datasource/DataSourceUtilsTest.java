package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
    void 바인딩을_해제한다() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Nested
    class GetConnection {

        @Test
        void 트랜잭션에_바인딩된_커넥션이_있으면_그_커넥션을_반환한다() throws SQLException {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            final Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isSameAs(connection);
            verify(dataSource, never()).getConnection();
        }

        @Test
        void 바인딩된_커넥션이_없으면_DataSource에서_새로_얻고_바인딩하지_않는다() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);

            final Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        @Test
        void 커넥션을_얻지_못하면_원인_예외를_보존한_예외를_던진다() throws SQLException {
            final SQLException failure = new SQLException("connection failed");
            when(dataSource.getConnection()).thenThrow(failure);

            assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                    .isInstanceOf(CannotGetJdbcConnectionException.class)
                    .hasCause(failure);
        }
    }

    @Nested
    class ReleaseConnection {

        @Test
        void 트랜잭션에_바인딩된_커넥션은_닫지_않는다() throws SQLException {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection, never()).close();
        }

        @Test
        void 바인딩되지_않은_커넥션은_닫는다() throws SQLException {
            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection).close();
        }

        @Test
        void 커넥션을_닫지_못해도_예외를_던지지_않는다() throws SQLException {
            doThrow(new SQLException("close failed")).when(connection).close();

            assertThatCode(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                    .doesNotThrowAnyException();
        }
    }
}

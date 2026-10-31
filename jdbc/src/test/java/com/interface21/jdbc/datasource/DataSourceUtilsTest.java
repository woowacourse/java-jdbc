package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @DisplayName("일반 작업은 새 연결을 얻고 사용 후 닫으며 저장소에 남기지 않는다")
    void obtainsAndClosesUnboundConnection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("트랜잭션에 저장된 연결을 재사용하고 작업 도중에는 닫지 않는다")
    void reusesAndKeepsBoundConnectionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    @DisplayName("저장된 연결과 다른 연결은 닫는다")
    void closesConnectionThatIsNotBound() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @Test
    @DisplayName("연결을 얻지 못하면 원인을 담은 예외를 발생시킨다")
    void preservesConnectionFailure() throws SQLException {
        final var cause = new SQLException("Cannot connect");
        when(dataSource.getConnection()).thenThrow(cause);

        final var exception = assertThrows(CannotGetJdbcConnectionException.class,
                () -> DataSourceUtils.getConnection(dataSource));

        assertThat(exception).hasCause(cause);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("연결 닫기에 실패하면 원인을 담은 예외를 발생시킨다")
    void preservesCloseFailure() throws SQLException {
        final var cause = new SQLException("Cannot close");
        doThrow(cause).when(connection).close();

        final var exception = assertThrows(CannotGetJdbcConnectionException.class,
                () -> DataSourceUtils.releaseConnection(connection, dataSource));

        assertThat(exception).hasCause(cause);
    }
}

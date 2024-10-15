package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    DataSource dataSource;
    Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @DisplayName("이미 bind된 Connection이 있으면, 해당 Connection을 반환한다.")
    @Test
    void getConnection_When_AlreadyBinded() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection returnedConnection = DataSourceUtils.getConnection(dataSource);

        // then
        Assertions.assertAll(
                () -> assertThat(returnedConnection).isEqualTo(connection),
                () -> verify(dataSource, never()).getConnection()
        );
    }

    @DisplayName("Connection이 bind되지 않았다면, Connection을 bind한 뒤 반환한다.")
    @Test
    void getConnection_When_Unbinded() {
        // when
        Connection returnedConnection = DataSourceUtils.getConnection(dataSource);

        // then
        Assertions.assertAll(
                () -> assertThat(returnedConnection).isNotEqualTo(connection),
                () -> verify(dataSource, times(1)).getConnection()
        );
    }

    @DisplayName("새롭게 bind된 Connection을 반환할 수 있다.")
    @Test
    void getConnection_bind_newConnection() {
        // when
        Connection returnedConnection = DataSourceUtils.getConnection(dataSource);
        Connection bindedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        Assertions.assertAll(
                () -> assertThat(returnedConnection).isEqualTo(bindedConnection)
        );
    }

    @DisplayName("SQLException 발생시 변환된 예외를 던진다.")
    @Test
    void getConnection_ThrowsException() throws SQLException {
        // given
        when(dataSource.getConnection()).thenThrow(new SQLException("Test Exception"));

        // when && then
        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class);
    }

    @DisplayName("null인 Connection을 release시, 아무 동작을 하지 않는다.")
    @Test
    void releaseConnection_NullConnection() throws SQLException {
        // given && when
        DataSourceUtils.releaseConnection(null, dataSource);

        // then
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @DisplayName("Connection을 release시, ConnectionManager로 unbind하고 close()를 호출한다.")
    @Test
    void releaseConnection() throws SQLException {
        // given
        when(connection.isClosed()).thenReturn(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);
        Connection boundConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertAll(
                () -> assertThat(boundConnection).isNull(),
                () -> verify(connection).close()
        );
    }

    @DisplayName("Connection이 이미 close된 상태일 때, close()를 호출하지 않는다.")
    @Test
    void testReleaseConnection_AlreadyClosed() throws SQLException {
        // given
        when(connection.isClosed()).thenReturn(true);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);

        // then
        verify(connection, never()).close();
    }
}

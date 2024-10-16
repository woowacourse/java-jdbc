package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
    }

    @Test
    @DisplayName("데이터 소스에서 Connection 객체를 가져온다.")
    void getConnection() throws SQLException {
        // given
        when(dataSource.getConnection()).thenReturn(connection);

        // when
        Connection retrievedConnection = DataSourceUtils.getConnection(dataSource);

        // then
        assertThat(retrievedConnection).isSameAs(connection);
    }

    @Test
    @DisplayName("이미 바인딩된 Connection 객체를 가져온다.")
    void getConnection_AlreadyBound() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection retrievedConnection = DataSourceUtils.getConnection(dataSource);

        // then
        assertThat(retrievedConnection).isSameAs(connection);
        verify(dataSource, never()).getConnection();
    }

    @Test
    @DisplayName("Connection 객체를 가져오는 중 SQLException이 발생하면 CannotGetJdbcConnectionException을 던진다.")
    void getConnection_SqlException() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        // when & then
        assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to obtain JDBC Connection");
    }

    @Test
    @DisplayName("Connection 객체를 해제한다.")
    void releaseConnection() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        DataSourceUtils.releaseConnection(connection, dataSource);

        // then
        verify(connection, times(1)).close();

        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(retrievedConnection).isNull();
    }

    @Test
    @DisplayName("Connection 객체 해제 중 SQLException이 발생하면 CannotGetJdbcConnectionException을 던진다.")
    void releaseConnection_SqlException() throws SQLException {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        doThrow(new SQLException("Close error")).when(connection).close();

        // when & then
        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to close JDBC Connection");
    }
}

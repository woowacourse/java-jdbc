package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    public void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
    }

    @AfterEach
    public void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @DisplayName("이미 커넥션이 존재하는 경우 기존 커넥션을 사용한다.")
    @Test
    public void getConnectionAlreadyExist() {
        Connection result = DataSourceUtils.getConnection(dataSource);

        assertThat(result).isEqualTo(connection);
    }

    @DisplayName("DataSource에 바인드된 커넥션이 없는 경우 새로 생성한다.")
    @Test
    public void getNewConnection() throws SQLException {
        DataSource newDataSource = Mockito.mock(DataSource.class);
        Connection newConnection = Mockito.mock(Connection.class);
        when(newDataSource.getConnection()).thenReturn(newConnection);

        Connection result = DataSourceUtils.getConnection(newDataSource);

        assertThat(result).isEqualTo(newConnection);
        TransactionSynchronizationManager.unbindResource(newDataSource);
    }

    @DisplayName("커넥션을 가져오는 데 실패하면 예외가 발생한다.")
    @Test
    public void failToGetConnection() throws SQLException {
        DataSource newDataSource = Mockito.mock(DataSource.class);
        when(newDataSource.getConnection()).thenThrow(new SQLException("DB 연결 실패"));

        assertThatThrownBy(() -> DataSourceUtils.getConnection(newDataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to obtain JDBC Connection");
    }

    @DisplayName("커넥션을 해제한다.")
    @Test
    public void releaseConnection() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @DisplayName("커넥션을 닫는 도중에 예외가 발생하면 CannotGetJdbcConnectionException을 던져야 한다.")
    @Test
    public void failToCloseConnection() throws SQLException {
        doThrow(new SQLException("닫기 실패"))
                .when(connection).close();

        assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasMessageContaining("Failed to close JDBC Connection");
    }
}

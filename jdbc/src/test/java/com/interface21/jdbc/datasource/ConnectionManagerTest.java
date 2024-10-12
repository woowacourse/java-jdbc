package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionManagerTest {

    @DisplayName("커넥션을 가져온다")
    @Test
    void getConnectionSuccess() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        ConnectionManager connectionManager = new ConnectionManager(dataSource);

        doReturn(connection).when(dataSource).getConnection();

        assertThat(connectionManager.getConnection()).isEqualTo(connection);
    }

    @DisplayName("커넥션을 가져오는 과정에서 예외를 전환한다")
    @Test
    void getConnectionFail() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        ConnectionManager connectionManager = new ConnectionManager(dataSource);

        doThrow(SQLException.class).when(dataSource).getConnection();

        assertThatThrownBy(connectionManager::getConnection)
                .isInstanceOf(DataAccessException.class);
    }
}

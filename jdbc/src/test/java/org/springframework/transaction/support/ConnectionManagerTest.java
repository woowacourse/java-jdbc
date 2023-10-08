package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConnectionManagerTest {

    private ConnectionManager connectionManager;
    private DataSource datasource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        connectionManager = ConnectionManager.getInstance();
        datasource = mock(DataSource.class);
        connection = spy(Connection.class);
        when(datasource.getConnection()).thenReturn(connection);
    }

    @Nested
    @DisplayName("datasource로 connection을 맞는다.")
    class getConnection {

        @Test
        @DisplayName("스레드에서 connection을 생성한 적이 없다면, tx를 생성한다.")
        void getNewConnection() throws SQLException {
            final Connection connection = connectionManager.getConnection(datasource);

            assertThat(connection)
                .isNotNull();
        }

        @Test
        @DisplayName("스레드에서 connection을 생성한 적이 있다면, 기존 tx를 반환한다.")
        void getOldConnection() throws SQLException {
            final Connection oldTransaction = connectionManager.getConnection(datasource);

            final Connection newTransaction = connectionManager.getConnection(datasource);

            assertThat(oldTransaction)
                .isEqualTo(newTransaction);
        }
    }

    @Test
    @DisplayName("beginTransaction을 호출하고, getTransaction을 호출하면,setAutocommit을 false로 만든다.")
    void beginTransaction() throws SQLException {
        connectionManager.beginTransaction();

        final Connection conn = connectionManager.getConnection(datasource);

        verify(conn, times(1)).setAutoCommit(false);
    }

    @Test
    @DisplayName("connection을 close한다")
    void closeConnection() throws SQLException {
        final Connection conn = connectionManager.getConnection(datasource);

        connectionManager.close();

        verify(conn, times(1)).close();
    }

    @Test
    @DisplayName("connection을 commit한다")
    void commit() throws SQLException {
        final Connection conn = connectionManager.getConnection(datasource);

        connectionManager.commit();

        verify(conn, times(1)).commit();
    }

    @Test
    @DisplayName("connection을 rollback한다")
    void rollback() throws SQLException {
        final Connection conn = connectionManager.getConnection(datasource);

        connectionManager.rollback();

        verify(conn, times(1)).rollback();
    }
}

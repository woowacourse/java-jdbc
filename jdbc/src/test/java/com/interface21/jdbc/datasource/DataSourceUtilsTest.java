package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("트랜잭션에 바인딩된 커넥션이 있으면 그 커넥션을 돌려준다")
    void returnsBoundConnection() throws SQLException {
        final Connection bound = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, bound);

        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(bound);
        verify(dataSource, never()).getConnection();
    }

    @Test
    @DisplayName("바인딩된 커넥션이 없으면 새로 꺼내고 바인딩하지 않는다")
    void returnsNewConnectionWithoutBinding() {
        assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("바인딩된 커넥션은 반납해도 닫지 않는다")
    void doesNotCloseBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection, never()).close();
    }

    @Test
    @DisplayName("바인딩되지 않은 커넥션은 반납하면 닫는다")
    void closesUnboundConnection() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }
}

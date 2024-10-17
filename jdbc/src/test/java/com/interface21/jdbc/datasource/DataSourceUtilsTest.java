package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DataSourceUtilsTest {

    private Connection connection;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("커넥션이 있으면 해당 커넥션을 반환한다.")
    @Test
    void getConnection() throws SQLException {
        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.getResource(dataSource)).thenReturn(connection);
        }

        assertThat(DataSourceUtils.getConnection(dataSource)).isEqualTo(connection);
    }

    @DisplayName("커넥션을 닫는다.")
    @Test
    void releaseConnection() throws SQLException {
        DataSourceUtils.releaseConnection(connection, dataSource);

        verify(connection).close();
    }

    @DisplayName("트랜잭션 안에 있는 커넥션이면 false를 반환한다")
    @Test
    void isTransactionActive() throws SQLException {
        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.getResource(dataSource)).thenReturn(connection);
        }

        assertThat(DataSourceUtils.isTransactionNotActive(connection, dataSource)).isFalse();
    }
}

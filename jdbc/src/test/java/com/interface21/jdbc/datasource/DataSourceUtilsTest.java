package com.interface21.jdbc.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
    }

    @DisplayName("이미 bind된 Connection을 가져온다.")
    @Test
    void getConnection_AlreadyBound() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(DataSourceUtils.getConnection(dataSource)).isEqualTo(connection);
    }

    @DisplayName("bind되지 않은 Connection을 새로 bind하고 가져온다.")
    @Test
    void getConnection_NotBound() throws SQLException {
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);

        assertAll(
                () -> assertThat(DataSourceUtils.getConnection(dataSource)).isEqualTo(connection),
                () -> assertThat(TransactionSynchronizationManager.manages(dataSource)).isTrue()
        );
    }

    @DisplayName("Connection 자원을 해제한다.")
    @Test
    void releaseConnection() {
        DataSourceUtils.releaseConnection(connection, dataSource);

        assertAll(
                () -> verify(connection).close(),
                () -> assertThat(TransactionSynchronizationManager.manages(dataSource)).isFalse()
        );
    }
}

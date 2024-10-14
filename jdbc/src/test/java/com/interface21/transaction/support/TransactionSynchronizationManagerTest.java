package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
    }

    @DisplayName("DataSource로부터 Connection을 가져온다 (bound)")
    @Test
    void getResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(connection);
    }

    @DisplayName("DataSource로부터 Connection을 가져온다 (not bound)")
    @Test
    void getResource_Null() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @DisplayName("DataSource와 Connection 매핑을 추가한다.")
    @Test
    void bindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.manages(dataSource)).isTrue();
    }

    @DisplayName("DataSource와 Connection 매핑을 제거한다.")
    @Test
    void unbindResource() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
        assertThat(TransactionSynchronizationManager.manages(dataSource)).isFalse();
        connection.close();
    }
}

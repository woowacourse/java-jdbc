package com.interface21.transaction.support;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("dataSource의 connection이 없으면 null을 반환한다.")
    void get_returnNull_noConnectionExist() {
        // when & then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("dataSource의 connection과 연결되어 있으면 해당 connection을 반환한다.")
    void get_returnConnection() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when & then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(connection);
    }

    @Test
    @DisplayName("unbind한 이후에는 null을 반환한다.")
    void unbind_returnNull() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.unbindResource(dataSource);

        // when & then
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}

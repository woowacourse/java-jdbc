package com.interface21.transaction.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {


    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @DisplayName("Datasorce를 키값으로 Connection을 가져온다")
    @Test
    void getResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertEquals(connection, TransactionSynchronizationManager.getResource(dataSource));
    }

    @DisplayName("Datasorce를 키값으로 Connection을 바인딩한다")
    @Test
    void bindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertEquals(connection, TransactionSynchronizationManager.getResource(dataSource));
    }

    @DisplayName("이미 Connection이 바인딩되어 있을 때 다시 바인딩하면 IllegalStateException을 발생시킨다")
    @Test
    void bindResourceWithAlreadyBoundConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThrows(
                IllegalStateException.class,
                () -> TransactionSynchronizationManager.bindResource(dataSource, connection)
        );
    }

    @DisplayName("Datasorce를 키값으로 Connection을 언바인딩한다")
    @Test
    void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertEquals(connection, TransactionSynchronizationManager.unbindResource(dataSource));
        assertNull(TransactionSynchronizationManager.getResource(dataSource));
    }

    @DisplayName("Connection이 바인딩되어 있지 않을 때 언바인딩하면 IllegalStateException을 발생시킨다")
    @Test
    void unbindResourceWithNotBoundConnection() {
        DataSource newDataSource = mock(DataSource.class);

        assertThrows(
                IllegalStateException.class,
                () -> TransactionSynchronizationManager.unbindResource(newDataSource)
        );
    }
}

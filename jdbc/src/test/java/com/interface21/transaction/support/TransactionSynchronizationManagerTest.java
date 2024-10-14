package com.interface21.transaction.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TransactionSynchronizationManagerTest {

    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setup() {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
    }

    @Test
    void bind_getResource() {
        //given
        TransactionSynchronizationManager.bindResource(mockDataSource, mockConnection);

        //when
        Connection connection = TransactionSynchronizationManager.getResource(mockDataSource);

        //then
        assertAll(
                () -> assertNotNull(connection),
                () -> assertEquals(mockConnection, connection)
        );
    }

    @Test
    void getResource_isNull() {
        //when
        Connection connection = TransactionSynchronizationManager.getResource(mockDataSource);

        //then
        assertNull(connection);
    }

    @Test
    void unbindResource() {
        //given
        TransactionSynchronizationManager.bindResource(mockDataSource, mockConnection);

        //when
        Connection removedConnection = TransactionSynchronizationManager.unbindResource(mockDataSource);

        //then
        assertAll(
                () -> assertNotNull(removedConnection),
                () -> assertEquals(mockConnection, removedConnection)
        );
    }

    @Test
    void unbindResource_withNoSuchElement() {
        //when, then
        String message = assertThrows(NoSuchElementException.class, () ->
                TransactionSynchronizationManager.unbindResource(mockDataSource)
        ).getMessage();

        //then
        assertThat(message).contains("존재");
    }

    @Test
    void resourcesCleanUp() {
        //given
        TransactionSynchronizationManager.bindResource(mockDataSource, mockConnection);

        //when
        TransactionSynchronizationManager.unbindResource(mockDataSource);

        //then
        assertNull(TransactionSynchronizationManager.getResource(mockDataSource));
    }
}

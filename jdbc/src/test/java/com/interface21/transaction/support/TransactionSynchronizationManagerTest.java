package com.interface21.transaction.support;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionSynchronizationManagerTest {
    private DataSource mockDataSource;
    private Connection mockConnection;

    @BeforeEach
    void setUp() {
        mockDataSource = Mockito.mock(DataSource.class);
        mockConnection = Mockito.mock(Connection.class);

        TransactionSynchronizationManager.unbindResource(mockDataSource);
    }

    @DisplayName("리소스를 바인딩한 후 커넥션을 요청하면 매번 같은 커넥션을 반환한다.")
    @Test
    void testBindAndGetResource() {
        TransactionSynchronizationManager.bindResource(mockDataSource, mockConnection);

        Connection connection1 = TransactionSynchronizationManager.getResource(mockDataSource);
        Connection connection2 = TransactionSynchronizationManager.getResource(mockDataSource);

        assertAll(
                () -> assertThat(connection1)
                        .isEqualTo(mockConnection),
                () -> assertThat(connection1)
                        .isEqualTo(connection2)
        );
    }

    @DisplayName("리소스를 언바인딩한 후 커넥션을 요청하면 null을 반환한다.")
    @Test
    void testUnbindResource() {
        TransactionSynchronizationManager.bindResource(mockDataSource, mockConnection);
        TransactionSynchronizationManager.unbindResource(mockDataSource);

        Connection connection = TransactionSynchronizationManager.getResource(mockDataSource);

        assertThat(connection)
                .isNull();
    }

    @DisplayName("리소스를 바인딩하지 않고 커넥션을 요청하면 null을 반환한다.")
    @Test
    void testGetResourceWhenNotBound() {
        Connection connection = TransactionSynchronizationManager.getResource(mockDataSource);
        assertThat(connection)
                .isNull();
    }
}

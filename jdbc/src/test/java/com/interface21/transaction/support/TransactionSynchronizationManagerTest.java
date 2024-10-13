package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
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
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
    }

    @Test
    @DisplayName("현재 스레드에 트랜잭션이 활성화되어 있는지 확인한다.")
    void isActive() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        boolean isActive = TransactionSynchronizationManager.isActive(dataSource);

        // then
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("Connection 객체를 보관하고 가져온다.")
    void bindAndGetResource() {
        // when
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(retrievedConnection).isSameAs(connection);
    }

    @Test
    @DisplayName("Connection 객체를 해제한다.")
    void unbindResource() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(unboundConnection).isSameAs(connection);

        Connection retrievedConnection = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(retrievedConnection).isNull();
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("바인딩한 커넥션을 같은 DataSource로 조회할 수 있다")
    void getBoundResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("바인딩하지 않은 DataSource를 조회하면 null을 반환한다")
    void getUnboundResource() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("DataSource마다 커넥션을 따로 바인딩한다")
    void bindResourcePerDataSource() {
        final DataSource otherDataSource = mock(DataSource.class);
        final Connection otherConnection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    @DisplayName("이미 바인딩된 DataSource에 다시 바인딩하면 예외를 던지고 기존 커넥션을 유지한다")
    void bindResourceTwice() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("바인딩을 해제하면 해제한 커넥션을 반환하고 이후 조회는 null이다")
    void unbindResource() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("바인딩하지 않은 DataSource를 해제하면 null을 반환한다")
    void unbindUnboundResource() {
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("다른 스레드에서는 바인딩한 커넥션이 보이지 않는다")
    void resourceIsThreadLocal() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection fromOtherThread = CompletableFuture
                .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                .join();

        assertThat(fromOtherThread).isNull();
    }
}

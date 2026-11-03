package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("데이터소스별 연결을 독립적으로 관리한다")
    void bindAndUnbindConnectionsByDataSource() {
        DataSource first = mock(DataSource.class);
        DataSource second = mock(DataSource.class);
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(first, firstConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(first, secondConnection))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
    }

    @Test
    @DisplayName("스레드 간 트랜잭션 연결이 섞이지 않는다")
    void isolateConnectionsBetweenThreads() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Connection otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            CompletableFuture.runAsync(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).join();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("스레드와 DataSource별 트랜잭션 연결 동기화")
class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    @DisplayName("등록하지 않은 DataSource의 연결은 조회와 해제 모두 null을 반환한다")
    void returnsNullWhenNoConnectionIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("현재 스레드에 등록한 연결을 같은 객체로 조회한다")
    void returnsBoundConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("동일한 DataSource에 중복 등록하면 기존 연결을 보존하고 예외를 던진다")
    void rejectsDuplicateBindingWithoutReplacingConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var replacement = mock(Connection.class);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, replacement))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("DataSource별로 연결을 구분하고 하나를 해제해도 다른 연결은 유지한다")
    void separatesConnectionsByDataSource() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    @DisplayName("마지막 연결을 해제한 뒤 같은 스레드에 새 연결을 등록할 수 있다")
    void bindsFreshConnectionAfterUnbinding() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        final var replacement = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, replacement);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(replacement);
    }

    @Test
    @DisplayName("동일한 DataSource라도 서로 다른 스레드의 연결은 섞이지 않는다")
    void isolatesConnectionsBetweenThreads() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var result = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    return TransactionSynchronizationManager.getResource(dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get();

            assertThat(result).isSameAs(otherConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get()).isNull();
        }
    }
}

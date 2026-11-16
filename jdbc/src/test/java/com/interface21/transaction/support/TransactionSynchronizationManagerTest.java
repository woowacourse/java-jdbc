package com.interface21.transaction.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @DisplayName("같은 데이터소스라도 다른 스레드에 등록된 연결은 조회되지 않는다")
    @Test
    void connectionIsIsolatedByThread() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var lookup = new FutureTask<>(() -> TransactionSynchronizationManager.getResource(dataSource));

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            Thread.ofVirtual().start(lookup);

            assertThat(lookup.get(5, TimeUnit.SECONDS)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @DisplayName("한 데이터소스의 연결을 해제해도 다른 데이터소스의 연결은 유지된다")
    @Test
    void connectionsAreIsolatedByDataSource() {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var firstConnection = mock(Connection.class);
        final var secondConnection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(first, firstConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try {
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
    }
}

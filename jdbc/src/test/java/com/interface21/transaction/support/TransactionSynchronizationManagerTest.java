package com.interface21.transaction.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("등록된 연결은 같은 스레드의 같은 데이터소스에서만 조회된다")
    void scopesConnectionToThreadAndDataSource() throws Exception {
        // given
        final var dataSource = mock(DataSource.class);
        final var otherDataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            // when
            final var localConnection = TransactionSynchronizationManager.getResource(dataSource);
            final var otherSourceConnection = TransactionSynchronizationManager.getResource(otherDataSource);
            final var otherThreadLookup = new FutureTask<>(() -> TransactionSynchronizationManager.getResource(dataSource));
            Thread.ofVirtual().start(otherThreadLookup);

            // then
            assertThat(localConnection).isSameAs(connection);
            assertThat(otherSourceConnection).isNull();
            assertThat(otherThreadLookup.get(5, TimeUnit.SECONDS)).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("같은 데이터소스에 두 번째 연결을 등록해도 기존 연결이 유지된다")
    void rejectsDuplicateBinding() {
        // given
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            // when
            assertThrows(IllegalStateException.class,
                    () -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection));

            // then
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

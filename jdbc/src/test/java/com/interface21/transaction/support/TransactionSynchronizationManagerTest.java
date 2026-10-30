package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("저장된 연결이 없으면 null을 반환한다")
    void returnsNullWhenNoConnectionIsBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("저장한 연결을 조회하고 제거한 뒤 다시 저장할 수 있다")
    void bindsAndUnbindsConnection() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var nextConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, nextConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(nextConnection);
    }

    @Test
    @DisplayName("다른 데이터소스의 연결은 따로 저장하고 제거한다")
    void keepsConnectionsSeparateByDataSource() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        try {
            TransactionSynchronizationManager.unbindResource(dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    @DisplayName("이미 연결이 있으면 덮어쓰지 않고 예외를 발생시킨다")
    void rejectsDuplicateBinding() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThrows(IllegalStateException.class,
                () -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)));

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("저장되지 않은 연결을 제거하면 예외를 발생시킨다")
    void rejectsUnbindingMissingConnection() {
        assertThrows(IllegalStateException.class,
                () -> TransactionSynchronizationManager.unbindResource(dataSource));
    }

    @Test
    @DisplayName("같은 데이터소스도 스레드가 다르면 연결을 따로 저장한다")
    void keepsConnectionsSeparateByThread() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);

        try (var executor = Executors.newSingleThreadExecutor()) {
            final var result = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    return TransactionSynchronizationManager.getResource(dataSource);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            });

            assertThat(result.get(5, TimeUnit.SECONDS)).isSameAs(otherConnection);
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}

package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Connection otherConnection = mock(Connection.class);

    @AfterEach
    void cleanUp() {
        TransactionSynchronizationManager.unbindResource(dataSource);
        TransactionSynchronizationManager.unbindResource(otherDataSource);
    }

    @Test
    @DisplayName("등록된 커넥션이 없으면 조회와 해제는 null을 반환한다")
    void returnsNullWhenResourceIsNotBound() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("등록한 커넥션을 조회하고 해제한 뒤 다시 등록할 수 있다")
    void bindsUnbindsAndBindsAgain() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(otherConnection);
    }

    @Test
    @DisplayName("한 데이터소스의 커넥션을 해제해도 다른 데이터소스의 커넥션은 유지된다")
    void unbindingOneResourcePreservesOtherResources() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    @DisplayName("동일 데이터소스를 중복 등록하면 실패하고 기존 커넥션이 유지된다")
    void rejectsDuplicateBinding() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, otherConnection))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("같은 데이터소스라도 스레드마다 서로 다른 커넥션을 사용한다")
    void isolatesResourcesByThread() throws Exception {
        final var bothBound = new CountDownLatch(2);
        try (final var executor = Executors.newFixedThreadPool(2)) {
            final var first = executor.submit(() -> bindAndReadInThread(connection, bothBound));
            final var second = executor.submit(() -> bindAndReadInThread(otherConnection, bothBound));

            assertThat(first.get(10, TimeUnit.SECONDS)).isSameAs(connection);
            assertThat(second.get(10, TimeUnit.SECONDS)).isSameAs(otherConnection);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }
    }

    private Connection bindAndReadInThread(final Connection threadConnection, final CountDownLatch bothBound)
            throws InterruptedException {
        TransactionSynchronizationManager.bindResource(dataSource, threadConnection);
        try {
            bothBound.countDown();
            if (!bothBound.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("두 스레드가 커넥션을 등록하지 못했습니다.");
            }
            return TransactionSynchronizationManager.getResource(dataSource);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void bindResource로_등록한_커넥션을_getResource로_조회할_수_있다() {
        final Connection connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isEqualTo(connection);
    }

    @Test
    void unbindResource로_제거한_이후에는_조회되지_않는다() {
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final Connection unbound = TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(unbound).isEqualTo(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 이미_커넥션이_바인딩된_상태에서_다시_바인딩하면_예외가_발생한다() {
        TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 스레드마다_바인딩된_커넥션이_서로_분리된다() throws InterruptedException {
        final int threadCount = 10;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final List<Connection> boundConnections = new CopyOnWriteArrayList<>();
        final List<Boolean> visibleToOtherThreads = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    final Connection connection = mock(Connection.class);
                    TransactionSynchronizationManager.bindResource(dataSource, connection);

                    final Connection resolved = TransactionSynchronizationManager.getResource(dataSource);
                    boundConnections.add(resolved);
                    visibleToOtherThreads.add(resolved == connection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(boundConnections).hasSize(threadCount);
        assertThat(visibleToOtherThreads).containsOnly(true);
        assertThat(boundConnections).doesNotHaveDuplicates();
    }
}

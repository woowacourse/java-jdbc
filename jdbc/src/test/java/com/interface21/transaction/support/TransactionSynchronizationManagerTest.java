package com.interface21.transaction.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("서로 다른 DataSource의 연결을 독립적으로 등록하고 해제한다")
    void differentDataSourcesHaveIndependentBindings() {
        final var first = mock(DataSource.class);
        final var second = mock(DataSource.class);
        final var firstConnection = mock(Connection.class);
        final var secondConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(first, firstConnection);
        TransactionSynchronizationManager.bindResource(second, secondConnection);
        try {
            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
            assertThat(TransactionSynchronizationManager.unbindResource(first)).isSameAs(firstConnection);
            assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            unbindIfPresent(first);
            unbindIfPresent(second);
        }
    }

    @Test
    @DisplayName("중복 등록을 거부하면서 먼저 등록한 연결을 보존한다")
    void duplicateBindingNeverReplacesTheExistingConnection() {
        final var dataSource = mock(DataSource.class);
        final var original = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, original);
        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(original);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("등록되지 않은 연결은 조회 결과가 없고 해제할 수 없다")
    void missingBindingCannotBeUnbound() {
        final var dataSource = mock(DataSource.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("null 키나 연결은 등록하지 않는다")
    void invalidInputsNeverCreateABinding() {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(null, connection))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TransactionSynchronizationManager.getResource(null))
                .isInstanceOf(NullPointerException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("같은 DataSource여도 다른 스레드에는 연결이 공유되지 않는다")
    void differentThreadsHaveIndependentConnectionsForTheSameDataSource() throws Exception {
        final var dataSource = mock(DataSource.class);
        final var parentConnection = mock(Connection.class);
        final var workerConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, parentConnection);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            final var worker = executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, workerConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(workerConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            });
            worker.get(5, TimeUnit.SECONDS);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(parentConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("스레드 풀의 다음 작업에는 이전 작업에서 해제한 연결이 남지 않는다")
    void reusedWorkerThreadDoesNotRetainThePreviousBinding() throws Exception {
        final var dataSource = mock(DataSource.class);
        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class));
                TransactionSynchronizationManager.unbindResource(dataSource);
            }).get(5, TimeUnit.SECONDS);

            final var nextBinding = executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .get(5, TimeUnit.SECONDS);

            assertThat(nextBinding).isNull();
        }
    }

    private void unbindIfPresent(final DataSource dataSource) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

package com.interface21.transaction.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final DataSource otherDataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @AfterEach
    void tearDown() {
        unbindIfPresent(dataSource);
        unbindIfPresent(otherDataSource);
    }

    @Test
    @DisplayName("등록한 커넥션을 조회하고 해제하면 더 이상 조회되지 않는다")
    void bindAndUnbindResource() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verifyNoInteractions(connection);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("하나의 데이터소스를 해제해도 다른 데이터소스의 커넥션은 유지한다")
    void resourcesAreSeparatedByDataSource() {
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
    }

    @Test
    @DisplayName("중복 등록은 거부하고 기존 커넥션을 유지한다")
    void rejectsDuplicateBinding() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(
                dataSource, mock(Connection.class)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("등록되지 않은 데이터소스는 해제할 수 없다")
    void rejectsUnbindingMissingResource() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("null 데이터소스나 커넥션은 등록할 수 없다")
    void rejectsNullResource() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(null, connection))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, null))
                .isInstanceOf(NullPointerException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("같은 데이터소스라도 스레드마다 별도의 커넥션을 보관한다")
    void resourcesAreSeparatedByThread() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var otherConnection = mock(Connection.class);

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                TransactionSynchronizationManager.bindResource(dataSource, otherConnection);
                try {
                    assertThat(TransactionSynchronizationManager.getResource(dataSource))
                            .isSameAs(otherConnection);
                } finally {
                    TransactionSynchronizationManager.unbindResource(dataSource);
                }
            }).get(5, TimeUnit.SECONDS);

            executor.submit(() -> assertThat(TransactionSynchronizationManager.getResource(dataSource))
                    .isNull()).get(5, TimeUnit.SECONDS);
        }

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    private void unbindIfPresent(final DataSource key) {
        if (TransactionSynchronizationManager.getResource(key) != null) {
            TransactionSynchronizationManager.unbindResource(key);
        }
    }
}

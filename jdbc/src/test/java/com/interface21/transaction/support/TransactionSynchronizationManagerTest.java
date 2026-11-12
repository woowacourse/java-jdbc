package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @Test
    void 묶인_Connection이_없으면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 묶은_Connection을_DataSource로_찾고_풀면_더는_찾을_수_없다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void DataSource마다_Connection을_따로_묶는다() {
        final var otherDataSource = mock(DataSource.class);
        final var otherConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        TransactionSynchronizationManager.bindResource(otherDataSource, otherConnection);

        try {
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(otherDataSource)).isSameAs(otherConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            TransactionSynchronizationManager.unbindResource(otherDataSource);
        }
    }

    @Test
    void 이미_묶인_DataSource에_다시_묶으면_예외를_던진다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 묶이지_않은_DataSource를_풀면_예외를_던진다() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 다른_스레드에서는_묶은_Connection을_볼_수_없다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            final var resourceInOtherThread = CompletableFuture
                    .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .join();

            assertThat(resourceInOtherThread).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

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
    void 바인딩한_Connection을_조회하고_해제할_수_있다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 바인딩된_Connection이_없으면_null을_반환한다() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 같은_DataSource에_두_번_바인딩하면_예외가_발생한다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, mock(Connection.class)))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void 바인딩되지_않은_DataSource를_해제하면_예외가_발생한다() {
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 다른_스레드에서는_바인딩된_Connection이_보이지_않는다() {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            final Connection fromOtherThread = CompletableFuture
                    .supplyAsync(() -> TransactionSynchronizationManager.getResource(dataSource))
                    .join();

            assertThat(fromOtherThread).isNull();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

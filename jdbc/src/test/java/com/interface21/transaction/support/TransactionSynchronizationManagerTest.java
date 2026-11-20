package com.interface21.transaction.support;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    void bindsSeparatelyForEachDataSourceAndPreventsReplacement() {
        DataSource first = mock(DataSource.class);
        DataSource second = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Connection other = mock(Connection.class);
        try {
            TransactionSynchronizationManager.bindResource(first, connection);
            TransactionSynchronizationManager.bindResource(second, other);

            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(connection);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(other);
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(first, other))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.getResource(first)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
        }
        assertThat(TransactionSynchronizationManager.getResource(first)).isNull();
    }

    @Test
    void isolatesConnectionsBetweenThreadsAndAllowsReuseAfterUnbind() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try (var executor = Executors.newSingleThreadExecutor()) {
            assertThat(executor.submit(() -> {
                assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
                Connection other = mock(Connection.class);
                TransactionSynchronizationManager.bindResource(dataSource, other);
                return TransactionSynchronizationManager.unbindResource(dataSource);
            }).get()).isNotSameAs(connection);
            assertThat(executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource)).get()).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceUtilsTest {

    @Test
    void unboundConnectionIsNotStoredAndIsReleased() {
        final var dataSource = pool();
        try {
            final var connection = DataSourceUtils.getConnection(dataSource);
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
            DataSourceUtils.releaseConnection(connection, dataSource);
            assertThat(dataSource.getActiveConnections()).isZero();
        } finally {
            dataSource.dispose();
        }
    }

    @Test
    void boundConnectionIsReusedUntilOwnerUnbindsIt() throws Exception {
        final var dataSource = pool();
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            assertThat(DataSourceUtils.getConnection(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
            assertThat(connection.isClosed()).isFalse();
            assertThat(dataSource.getActiveConnections()).isEqualTo(1);
            assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(TransactionSynchronizationManager.unbindResource(dataSource)).isSameAs(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
            assertThat(dataSource.getActiveConnections()).isZero();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
            dataSource.dispose();
        }
    }

    @Test
    void resourcesAreSeparatedByDataSource() throws Exception {
        final var first = pool();
        final var second = pool();
        try (var firstConnection = first.getConnection(); var secondConnection = second.getConnection()) {
            TransactionSynchronizationManager.bindResource(first, firstConnection);
            TransactionSynchronizationManager.bindResource(second, secondConnection);
            assertThat(DataSourceUtils.getConnection(first)).isSameAs(firstConnection);
            assertThat(DataSourceUtils.getConnection(second)).isSameAs(secondConnection);
            TransactionSynchronizationManager.unbindResource(first);
            assertThat(TransactionSynchronizationManager.getResource(second)).isSameAs(secondConnection);
        } finally {
            TransactionSynchronizationManager.unbindResource(first);
            TransactionSynchronizationManager.unbindResource(second);
            first.dispose();
            second.dispose();
        }
    }

    private JdbcConnectionPool pool() {
        final var pool = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID(), "", "");
        pool.setMaxConnections(1);
        pool.setLoginTimeout(1);
        return pool;
    }
}

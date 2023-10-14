package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isActive = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource dataSource) {
        if (!resources.get().containsKey(dataSource)) {
            final Connection connection = startNewTransaction(dataSource);
            resources.get().put(dataSource, connection);
        }
        return resources.get().get(dataSource);
    }

    public static Connection startNewTransaction(final DataSource dataSource) {
        isActive.set(true);
        return bindResource(dataSource);
    }

    private static Connection bindResource(final DataSource dataSource) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            resources.get().put(dataSource, connection);
            return connection;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void finishTransaction(final DataSource dataSource) {
        isActive.set(false);
        unbindResource(dataSource);
    }

    public static Connection unbindResource(final DataSource dataSource) {
        return resources.get().remove(dataSource);
    }
}

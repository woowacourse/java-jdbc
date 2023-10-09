package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> connectionMap = resources.get();
        return connectionMap != null ? connectionMap.get(key) : null;
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> connectionMap = new HashMap<>(Map.of(key, value));
        resources.set(connectionMap);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            return null;
        }
        final Connection connection = connectionMap.remove(key);
        if (connectionMap.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

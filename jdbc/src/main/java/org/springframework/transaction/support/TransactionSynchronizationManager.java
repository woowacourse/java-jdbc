package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        return dataSourceConnectionMap != null ? dataSourceConnectionMap.get(key) : null;
    }

    public static void bindResource(final DataSource key, final Connection value) {
        resources.set(new ConcurrentHashMap<>(Map.of(key, value)));
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}

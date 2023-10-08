package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources;

    static {
        resources = new ThreadLocal<>();
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> connections = resources.get();
        connections.put(key, value);
    }

    public static void unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();

        connections.remove(key);
    }
}

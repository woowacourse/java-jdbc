package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        return connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            resources.set(Map.of(key, value));
        } else {
            connections.put(key, value);
        }
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        return connections.remove(key);
    }
}

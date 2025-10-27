package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            return null;
        }
        return connections.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            return null;
        }
        final Connection removedConnection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return removedConnection;
    }
}

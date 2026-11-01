package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.containsKey(key)) {
            throw new IllegalStateException("Connection already bound to DataSource");
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final var connections = resources.get();
        if (connections == null || !connections.containsKey(key)) {
            throw new IllegalStateException("No Connection bound to DataSource");
        }
        final var connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

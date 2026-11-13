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
        if (connections == null) {
            return null;
        }
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to this DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        if (getResource(key) == null) {
            throw new IllegalStateException("No Connection bound to this DataSource");
        }
        final var connections = resources.get();
        final var connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

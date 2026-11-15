package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Map<DataSource, Connection> connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Objects.requireNonNull(value, "Connection must not be null");

        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.containsKey(key)) {
            throw new IllegalStateException("A connection is already bound to this DataSource");
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Map<DataSource, Connection> connections = resources.get();
        if (connections == null || !connections.containsKey(key)) {
            throw new IllegalStateException("No connection is bound to this DataSource");
        }

        Connection connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

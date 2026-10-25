package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to data source");
        }
    }

    public static Connection unbindResource(final DataSource key) {
        final var connections = resources.get();
        if (connections == null) {
            return null;
        }
        final var connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

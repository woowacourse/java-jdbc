package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource dataSource) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(dataSource);
    }

    public static void bindResource(final DataSource dataSource, final Connection connection) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(connection);
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(dataSource, connection) != null) {
            throw new IllegalStateException("A Connection is already bound to this DataSource");
        }
    }

    public static Connection unbindResource(final DataSource dataSource) {
        final var connections = resources.get();
        if (connections == null) {
            return null;
        }
        final var connection = connections.remove(dataSource);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

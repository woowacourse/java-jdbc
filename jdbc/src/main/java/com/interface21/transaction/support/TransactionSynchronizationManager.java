package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 현재 스레드의 DataSource별 트랜잭션 Connection을 보관한다.
 */
public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        Objects.requireNonNull(key, "dataSource must not be null");
        final var connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key, "dataSource must not be null");
        Objects.requireNonNull(value, "connection must not be null");
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("A Connection is already bound to this DataSource");
        }
    }

    public static Connection unbindResource(final DataSource key) {
        Objects.requireNonNull(key, "dataSource must not be null");
        final var connections = resources.get();
        final var connection = connections == null ? null : connections.remove(key);
        if (connection == null) {
            throw new IllegalStateException("No Connection is bound to this DataSource");
        }
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

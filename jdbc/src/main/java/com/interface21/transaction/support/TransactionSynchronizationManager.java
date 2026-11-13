package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "DataSource는 null일 수 없습니다.");
        Objects.requireNonNull(value, "Connection은 null일 수 없습니다.");

        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("해당 DataSource에 이미 Connection이 등록되어 있습니다.");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            return null;
        }

        final Connection connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

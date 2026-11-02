package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final var connections = resources.get();
        if (connections == null) {
            return null;
        }
        return connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }

        if (connections.containsKey(key)) {
            throw new IllegalStateException("이미 커넥션이 등록된 데이터소스입니다.");
        }
        connections.put(key, value);
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

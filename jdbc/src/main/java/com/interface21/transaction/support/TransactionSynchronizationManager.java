package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            return null;
        }

        return connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }

        if (connections.containsKey(key)) {
            throw new IllegalStateException("이미 등록된 커넥션이 있습니다.");
        }

        connections.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            return null;
        }

        Connection connection = connections.remove(key);

        if (connections.isEmpty()) {
            resources.remove();
        }

        return connection;
    }
}

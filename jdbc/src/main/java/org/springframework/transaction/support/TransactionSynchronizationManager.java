package org.springframework.transaction.support;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> connections = getConnections();
        return connections.get(key);
    }

    private static Map<DataSource, Connection> getConnections() {
        Map<DataSource, Connection> connections = resources.get();

        if (isNull(connections)) {
            connections = new HashMap<>();
            resources.set(connections);
        }

        return connections;
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> connections = getConnections();

        if (connections.containsKey(key)) {
            throw new IllegalStateException("이미 커넥션이 존재합니다.");
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = getConnections();
        final Connection connection = connections.get(key);

        if (isNull(connection)) {
            throw new IllegalStateException("커넥션이 존재하지 않습니다.");
        }
        connections.remove(key);

        if (connections.isEmpty()) {
            TransactionSynchronizationManager.resources.remove();
        }

        return connection;
    }
}

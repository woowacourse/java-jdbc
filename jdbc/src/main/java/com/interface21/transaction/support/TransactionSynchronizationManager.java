package com.interface21.transaction.support;

import java.util.HashMap;
import java.util.Optional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Optional<Connection> findConnection(DataSource dataSource) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(connections.get(dataSource));
    }

    public static void bindResource(DataSource dataSource, Connection connection) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }

        connections.put(dataSource, connection);
    }

    public static Connection unbindResource(DataSource dataSource) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null || !connections.containsKey(dataSource)) {
            throw new IllegalStateException("바인딩된 커넥션이 없습니다.");
        }

        Connection connection = connections.remove(dataSource);

        if (connections.isEmpty()) {
            resources.remove();
        }

        return connection;
    }
}

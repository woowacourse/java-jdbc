package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource dataSource) {
        Map<DataSource, Connection> connections = resources.get();

        if (connections == null) {
            return null;
        }

        return connections.get(dataSource);
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
        if (connections == null) {
            return null;
        }

        Connection connection = connections.remove(dataSource);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

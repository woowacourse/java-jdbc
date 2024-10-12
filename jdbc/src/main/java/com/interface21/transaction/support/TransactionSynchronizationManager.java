package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return getConnectionFromResources(key, () -> connections.get(key));
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return getConnectionFromResources(key, () -> connections.remove(key));
    }

    private static Connection getConnectionFromResources(DataSource key, Supplier<Connection> function) {
        Map<DataSource, Connection> connections = resources.get();
        if (connections != null && connections.containsKey(key)) {
            return function.get();
        }
        return null;
    }
}

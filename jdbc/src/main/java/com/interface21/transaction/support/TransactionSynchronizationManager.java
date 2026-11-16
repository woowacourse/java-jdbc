package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (connections == null) {
            return null;
        }
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (getResource(key) != null) {
            throw new IllegalStateException("이미 바인딩된 Connection이 있습니다.");
        }
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
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

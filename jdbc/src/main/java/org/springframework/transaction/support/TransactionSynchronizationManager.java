package org.springframework.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return connections.remove(key);
    }
}

package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            return null;
        }
        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            connectionMap = new HashMap<>();
            resources.set(connectionMap);
        }
        connectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Connection connection = getResource(key);
        resources.get().remove(key);
        return connection;
    }

    public static boolean isTransactionActive() {
        Map<DataSource, Connection> connectionMap = resources.get();
        return connectionMap != null && !connectionMap.isEmpty();
    }
}

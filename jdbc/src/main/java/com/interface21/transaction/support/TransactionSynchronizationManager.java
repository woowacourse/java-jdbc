package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connectionMap = resources.get();

        if (connectionMap == null || !connectionMap.containsKey(key)) {
            return null;
        }

        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.set(Map.of(key, value));
    }

    public static Connection unbindResource(DataSource key) {
        final Connection connection = getResource(key);
        resources.remove();
        return connection;
    }
}

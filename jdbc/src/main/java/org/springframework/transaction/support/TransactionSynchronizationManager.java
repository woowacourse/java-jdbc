package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionWithDataSource = resources.get();
        if (connectionWithDataSource == null) {
            return null;
        }
        return connectionWithDataSource.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionWithDataSource = resources.get();
        if (connectionWithDataSource == null) {
            connectionWithDataSource = new HashMap<>();
            resources.set(connectionWithDataSource);
        }
        connectionWithDataSource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionWithDataSource = resources.get();
        if (connectionWithDataSource == null) {
            return null;
        }
        return connectionWithDataSource.remove(key);
    }
}

package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.HashMap;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> currentResources = resources.get();
        return currentResources == null ? null : currentResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> currentResources = resources.get();
        if (currentResources == null) {
            currentResources = new HashMap<>();
            resources.set(currentResources);
        }
        if (currentResources.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound for DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> currentResources = resources.get();
        if (currentResources == null) {
            return null;
        }
        Connection connection = currentResources.remove(key);
        if (currentResources.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

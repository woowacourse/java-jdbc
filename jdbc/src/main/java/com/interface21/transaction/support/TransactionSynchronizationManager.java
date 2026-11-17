package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> boundResources = resources.get();
        if (boundResources == null) {
            return null;
        }
        return boundResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Objects.requireNonNull(value, "Connection must not be null");
        Map<DataSource, Connection> boundResources = resources.get();
        if (boundResources == null) {
            boundResources = new HashMap<>();
            resources.set(boundResources);
        }
        if (boundResources.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection is already bound to the current thread for " + key);
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> boundResources = resources.get();
        if (boundResources == null || !boundResources.containsKey(key)) {
            throw new IllegalStateException("No connection is bound to the current thread for " + key);
        }
        final Connection connection = boundResources.remove(key);
        // Pooled threads outlive requests, so an empty map must not stay attached to the thread.
        if (boundResources.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
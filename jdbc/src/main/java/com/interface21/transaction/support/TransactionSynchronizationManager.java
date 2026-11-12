package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> currentResources = resources.get();
        return currentResources == null ? null : currentResources.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Objects.requireNonNull(value, "Connection must not be null");

        Map<DataSource, Connection> currentResources = resources.get();
        if (currentResources != null && currentResources.containsKey(key)) {
            throw new IllegalStateException("A Connection is already bound to this DataSource");
        }
        if (currentResources == null) {
            currentResources = new HashMap<>();
            resources.set(currentResources);
        }
        currentResources.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> currentResources = resources.get();
        if (currentResources == null || !currentResources.containsKey(key)) {
            throw new IllegalStateException("No Connection is bound to this DataSource");
        }

        final Connection connection = currentResources.remove(key);
        if (currentResources.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

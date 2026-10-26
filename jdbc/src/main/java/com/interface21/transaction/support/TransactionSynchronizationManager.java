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
        Objects.requireNonNull(key, "DataSource must not be null");

        final var currentResources = resources.get();
        if (currentResources == null) {
            return null;
        }
        return currentResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Objects.requireNonNull(value, "Connection must not be null");

        var currentResources = resources.get();
        if (currentResources == null) {
            currentResources = new HashMap<>();
            resources.set(currentResources);
        }

        if (currentResources.containsKey(key)) {
            throw new IllegalStateException("A Connection is already bound to this DataSource");
        }
        currentResources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Objects.requireNonNull(key, "DataSource must not be null");

        final var currentResources = resources.get();
        if (currentResources == null || !currentResources.containsKey(key)) {
            throw new IllegalStateException("No Connection is bound to this DataSource");
        }

        final var connection = currentResources.remove(key);
        if (currentResources.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

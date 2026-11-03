package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var boundResources = resources.get();
        return boundResources == null ? null : boundResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var boundResources = resources.get();
        if (boundResources == null) {
            boundResources = new HashMap<>();
            resources.set(boundResources);
        }
        if (boundResources.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final var boundResources = resources.get();
        if (boundResources == null) {
            return null;
        }
        final var connection = boundResources.remove(key);
        if (boundResources.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

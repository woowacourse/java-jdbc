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
        final Map<DataSource, Connection> resourceMap = resources.get();
        return resourceMap == null ? null : resourceMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            resourceMap = new HashMap<>();
            resources.set(resourceMap);
        }
        if (resourceMap.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection is already bound to this DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            return null;
        }
        final Connection connection = resourceMap.remove(key);
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

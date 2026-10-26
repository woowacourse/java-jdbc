package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var resourceMap = RESOURCES.get();
        return resourceMap == null ? null : resourceMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(key, "DataSource must not be null");
        Objects.requireNonNull(value, "Connection must not be null");

        var resourceMap = RESOURCES.get();

        if (resourceMap == null) {
            resourceMap = new HashMap<>();
            RESOURCES.set(resourceMap);
        }

        if (resourceMap.containsKey(key)) {
            throw new IllegalStateException("이미 바인딩된 DataSource입니다.");
        }

        resourceMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final var resourceMap = RESOURCES.get();

        if (resourceMap == null) {
            return null;
        }

        final var connection = resourceMap.remove(key);

        if (resourceMap.isEmpty()) {
            RESOURCES.remove();
        }

        return connection;
    }
}

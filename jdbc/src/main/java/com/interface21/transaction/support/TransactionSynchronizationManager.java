package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();

        if (resourceMap == null) {
            return null;
        }

        return resourceMap.get(key);
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
            throw new IllegalStateException("해당 DataSource에 이미 Connection이 등록되어 있습니다.");
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();

        if (resourceMap == null) {
            return null;
        }

        Connection connection = resourceMap.remove(key);

        if (resourceMap.isEmpty()) {
            resources.remove();
        }

        return connection;
    }
}

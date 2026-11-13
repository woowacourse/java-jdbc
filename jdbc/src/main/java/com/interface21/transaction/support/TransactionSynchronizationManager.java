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
        var map = resources.get();
        return map == null ? null : map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        var map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        if (map.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("A connection is already bound to this data source");
        }
    }

    public static Connection unbindResource(final DataSource key) {
        var map = resources.get();
        if (map == null) {
            return null;
        }
        var connection = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static void unbindResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return;
        }
        map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
    }
}

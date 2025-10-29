package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        if (getResource(key) != null) {
            throw new IllegalStateException("Already bound resource, key: " + key);
        }

        final Map<DataSource, Connection> map = getOrCreateResourceMap();
        map.put(key, value);
    }

    private static Map<DataSource, Connection> getOrCreateResourceMap() {
        final Map<DataSource, Connection> map = resources.get();
        if (map != null) {
            return map;
        }

        final Map<DataSource, Connection> newMap = new HashMap<>();
        resources.set(newMap);
        return newMap;
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("Not bound resources map, key: " + key);
        }

        final Connection value = map.remove(key);
        if (value == null) {
            throw new IllegalStateException("Not bound connection, key: " + key);
        }

        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        initIfResourceIsNull();
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        return dataSourceConnection.getOrDefault(key, null);
    }

    public static void bindResource(DataSource key, Connection value) {
        initIfResourceIsNull();
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        dataSourceConnection.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        initIfResourceIsNull();
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        return dataSourceConnection.remove(key);
    }

    private static void initIfResourceIsNull() {
        if (resources.get() == null) {
            resources.set(new ConcurrentHashMap<>());
        }
    }
}

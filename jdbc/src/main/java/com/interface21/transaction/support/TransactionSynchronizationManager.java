package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection == null) {
            return null;
        }

        return dataSourceConnection.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection == null) {
            dataSourceConnection = new HashMap<>();
            resources.set(dataSourceConnection);
        }

        dataSourceConnection.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection == null || !dataSourceConnection.containsKey(key)) {
            throw new IllegalArgumentException("No Connection For DataSource");
        }

        return dataSourceConnection.remove(key);
    }
}

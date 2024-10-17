package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnections = resources.get();
        return dataSourceConnections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> dataSourceConnections = resources.get();
        dataSourceConnections.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnections = resources.get();
        dataSourceConnections.remove(key);
    }
}

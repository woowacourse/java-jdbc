package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionPool = RESOURCES.get();
        if (connectionPool.containsKey(key)) {
            return connectionPool.get(key);
        }
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionPool = RESOURCES.get();
        connectionPool.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = RESOURCES.get();
        return dataSourceConnectionMap.remove(key);
    }
}

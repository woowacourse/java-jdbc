package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot bind a null value to the thread");
        }
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap.containsKey(key)) {
            throw new IllegalStateException("Resource already bound to thread: " + key.getClass().getName());
        }
        connectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Connection removedConnection = resources.get().remove(key);
        if (removedConnection == null) {
            throw new IllegalStateException("No resource bound to thread");
        }
        return removedConnection;
    }
}

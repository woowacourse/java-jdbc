package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        Map<DataSource, Connection> connectionMap = resources.get();
        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = new HashMap<>();
        Connection mappedConnection = connectionMap.put(key, value);
        if (mappedConnection != null) {
            throw new IllegalStateException("fail to bind resource because of already exist bound thread");
        }
        resources.set(connectionMap);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        Connection connectionToRemove = connectionMap.get(key);
        resources.remove();
        return connectionToRemove;
    }
}

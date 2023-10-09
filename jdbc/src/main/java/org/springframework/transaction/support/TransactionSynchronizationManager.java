package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            return null;
        }
        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            connectionMap = new HashMap<>();
            resources.set(connectionMap);
        }

        Connection mappedConnection = connectionMap.put(key, value);
        if (mappedConnection != null) {
            throw new IllegalStateException("fail to bind resource because of already exist bound thread");
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            return null;
        }
        Connection connectionToRemove = connectionMap.remove(key);
        if (connectionToRemove == null) {
            throw new IllegalStateException("fail to unbind resource because of not found value");
        }
        if (connectionMap.isEmpty()) {
            resources.remove();
        }
        return connectionToRemove;
    }
}

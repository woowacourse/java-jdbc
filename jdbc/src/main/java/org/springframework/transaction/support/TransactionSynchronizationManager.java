package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (Objects.isNull(connectionMap)) {
            return null;
        }
        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (Objects.isNull(connectionMap)) {
            connectionMap = new HashMap<>();
        }
        connectionMap.put(key, value);
        resources.set(connectionMap);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> connectionMap = resources.get();
        return connectionMap.remove(key);
    }
}

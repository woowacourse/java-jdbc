package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionManager = resources.get();
        if (connectionManager == null) {
            connectionManager = new HashMap<>();
            resources.set(connectionManager);
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionManager = resources.get();
        if (connectionManager == null) {
            connectionManager = new HashMap<>();
            resources.set(connectionManager);
        }
        connectionManager.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}

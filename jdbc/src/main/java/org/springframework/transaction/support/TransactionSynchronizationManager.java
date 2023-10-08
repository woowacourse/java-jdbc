package org.springframework.transaction.support;

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
        Map<DataSource, Connection> connectionsByDataSource = resources.get();
        if (connectionsByDataSource == null) {
            return null;
        }
        return connectionsByDataSource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionsByDataSource = resources.get();

        if (connectionsByDataSource == null) {
            connectionsByDataSource = new HashMap<>();
            resources.set(connectionsByDataSource);
        }

        connectionsByDataSource.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionsByDataSource = resources.get();

        if (connectionsByDataSource == null) {
            return;
        }
        connectionsByDataSource.remove(key);
    }
}

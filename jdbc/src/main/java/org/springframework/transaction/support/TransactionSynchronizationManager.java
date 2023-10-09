package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connectionHolder = resources.get();

        if (connectionHolder == null) {
            return null;
        }

        return connectionHolder.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionHolder = resources.get();

        if (connectionHolder == null) {
            connectionHolder = new HashMap<>();
            resources.set(connectionHolder);
        }

        connectionHolder.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> connectionHolder = resources.get();

        if (connectionHolder == null) {
            return null;
        }

        return connectionHolder.remove(key);
    }
}

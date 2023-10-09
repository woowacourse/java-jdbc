package org.springframework.transaction.support;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static ConnectionHolder getResource(DataSource key) {
        final Map<DataSource, ConnectionHolder> connectionHolders = resources.get();
        if (connectionHolders == null) {
            return null;
        }
        return connectionHolders.get(key);
    }

    public static void bindResource(DataSource key, ConnectionHolder value) {
        Map<DataSource, ConnectionHolder> connectionHolders = resources.get();
        if (connectionHolders == null) {
            connectionHolders = new HashMap<>();
            resources.set(connectionHolders);
        }
        connectionHolders.put(key, value);
    }

    public static ConnectionHolder unbindResource(DataSource key) {
        final Map<DataSource, ConnectionHolder> connectionHolders = resources.get();
        if (connectionHolders == null) {
            return null;
        }
        return connectionHolders.remove(key);
    }
}

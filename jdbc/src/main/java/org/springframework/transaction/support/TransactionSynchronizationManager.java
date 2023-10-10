package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connectionByDataSource = resources.get();
        if(connectionByDataSource == null) {
            return null;
        }
        return connectionByDataSource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new ConcurrentHashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Connection removeConnection = resources.get().remove(key);
        resources.remove();
        return removeConnection;
    }

    public static boolean isActiveTransaction(DataSource key) {
        return getResource(key) != null;
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }

        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = new ConcurrentHashMap<>();
        connections.put(key, value);

        resources.set(connections);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();

        Connection removedConnection = connections.remove(key);
        resources.remove();

        return removedConnection;
    }

}

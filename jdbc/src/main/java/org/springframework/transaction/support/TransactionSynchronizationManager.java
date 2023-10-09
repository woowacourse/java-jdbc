package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<ConcurrentHashMap<DataSource, Connection>> resources = ThreadLocal.withInitial(ConcurrentHashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        ConcurrentHashMap<DataSource, Connection> connections = resources.get();
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        ConcurrentHashMap<DataSource, Connection> connections = new ConcurrentHashMap<>();
        connections.put(key, value);
        resources.set(connections);
    }

    public static Connection unbindResource(DataSource key) {
        final ConcurrentHashMap<DataSource, Connection> connections = resources.get();
        final Connection removedConnection = connections.remove(key);

        return removedConnection;
    }
}

package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =  ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = new HashMap<>();
        connections.put(key, value);
        resources.set(connections);
    }


    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        final Connection removedConnection = connections.remove(key);

        return removedConnection;
    }
}

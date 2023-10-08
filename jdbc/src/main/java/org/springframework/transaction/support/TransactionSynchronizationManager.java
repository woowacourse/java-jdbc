package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (Objects.nonNull(connections) && connections.containsKey(key)) {
            return connections.get(key);
        }
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        if (Objects.isNull(resources.get())) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Connection removedConnection = resources.get().remove(key);
        if (resources.get().isEmpty()) {
            resources.remove();
        }
        return removedConnection;
    }
}

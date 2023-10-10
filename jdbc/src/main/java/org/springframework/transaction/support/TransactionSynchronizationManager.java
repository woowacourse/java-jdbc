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
        Map<DataSource, Connection> connections = resources.get();
        if (Objects.isNull(connections)) {
            return null;
        }
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        if (Objects.isNull(connections)) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        connections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (Objects.isNull(resources.get())) {
            return null;
        }
        return resources.get().remove(key);
    }
}

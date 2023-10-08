package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static ThreadLocal<Map<DataSource, Connection>> resources;

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        if (resources == null) {
            return null;
        }

        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        if (resources == null) {
            resources = ThreadLocal.withInitial(HashMap::new);
        }

        final Map<DataSource, Connection> connections = resources.get();
        connections.put(key, value);
    }

    public static void unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();

        connections.remove(key);

        if (connections.isEmpty()) {
            resources.remove();
        }
    }
}

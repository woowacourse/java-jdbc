package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final var connections = resources.get();
        return connections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.set(Map.of(key, value));
    }

    public static Connection unbindResource(DataSource key) {
        final var connections = resources.get();
        final var connection = connections.get(key);
        resources.remove();

        return connection;
    }
}

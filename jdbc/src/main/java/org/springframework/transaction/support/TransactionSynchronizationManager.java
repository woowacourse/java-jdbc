package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource == null) {
            return null;
        }
        return resource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> connectionMap = new HashMap<>(Map.of(key, value));
        resources.set(connectionMap);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resource = resources.get();
        if (resource != null) {
            return resource.remove(key);
        }
        return null;
    }
}

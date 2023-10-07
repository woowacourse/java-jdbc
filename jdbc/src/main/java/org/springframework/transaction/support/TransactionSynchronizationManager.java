package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> resources = getResources();
        return resources.get(key);
    }

    private static Map<DataSource, Connection> getResources() {
        if (TransactionSynchronizationManager.resources.get() == null) {
            TransactionSynchronizationManager.resources.set(new HashMap<>());
        }

        return TransactionSynchronizationManager.resources.get();
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> resources = getResources();
        resources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> resources = getResources();
        return resources.remove(key);
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static boolean isSynchronizationActive() {
        return resources.get() != null;
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }

        Map<DataSource, Connection> map = resources.get();
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }

        Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            throw new IllegalStateException("No bound resource for key" + key);
        }
        Map<DataSource, Connection> map = resources.get();
        resources.remove();
        return map.remove(key);
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> localResources = getOrInitializeResources();
        return localResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> localResources = getOrInitializeResources();
        localResources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> localResources = getOrInitializeResources();
        return localResources.remove(key);
    }

    private static Map<DataSource, Connection> getOrInitializeResources() {
        Map<DataSource, Connection> localResources = resources.get();
        if (localResources == null) {
            localResources = new HashMap<>();
            resources.set(localResources);
        }
        return localResources;
    }
}

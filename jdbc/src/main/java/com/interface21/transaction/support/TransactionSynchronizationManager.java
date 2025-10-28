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
        Map<DataSource, Connection> threadResources = resources.get();
        if (threadResources == null) {
            return null;
        }
        return threadResources.get(key);

    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> threadResources = resources.get();
        if (threadResources == null) {
            threadResources = new HashMap<>();
            resources.set(threadResources);
        }
        threadResources.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> threadResources = resources.get();
        if (threadResources == null) {
            return null;
        }
        return threadResources.remove(key);
    }
}

package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resourceContainer = resources.get();
        if (resourceContainer == null) {
            return null;
        }
        return resourceContainer.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resourceContainer = resources.get();
        if (resourceContainer == null) {
            resourceContainer = new HashMap<>();
            resources.set(resourceContainer);
        }
        resourceContainer.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceContainer = resources.get();
        if (resourceContainer == null) {
            return null;
        }
        return resourceContainer.remove(key);
    }
}

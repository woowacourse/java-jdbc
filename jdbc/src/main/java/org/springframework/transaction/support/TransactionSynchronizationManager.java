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
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap != null) {
            return resourceMap.get(key);
        }
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            resourceMap = new HashMap<>();
            resources.set(resourceMap);
        }
        resourceMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        if (resourceMap == null) {
            return null;
        }
        Connection removedConnection = resourceMap.remove(key);
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
        return removedConnection;
    }
}

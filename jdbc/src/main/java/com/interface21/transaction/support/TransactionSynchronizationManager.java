package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        return resourceMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resourceMap = resources.get();
        resourceMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourceMap = resources.get();
        Connection connection = resourceMap.remove(key);
        removeIfEmpty(resourceMap);
        return connection;
    }

    private static void removeIfEmpty(Map<DataSource, Connection> resourceMap) {
        if (resourceMap.isEmpty()) {
            resources.remove();
        }
    }
}

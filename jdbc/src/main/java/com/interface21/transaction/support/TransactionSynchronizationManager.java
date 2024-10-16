package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> resourcesMap = TransactionSynchronizationManager.resources.get();
        if (resourcesMap.containsKey(key)) {
            return resourcesMap.get(key);
        }
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resourcesMap = resources.get();
        resourcesMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> resourcesMap = resources.get();
        return resourcesMap.remove(key);
    }
}

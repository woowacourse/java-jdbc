package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static boolean hasResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            return false;
        }

        return resources.get().containsKey(key);
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            return null;
        }

        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new HashMap<>();
            resources.set(dataSourceConnectionMap);
        }

        dataSourceConnectionMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }

        Connection removed = map.remove(key);

        if (map.isEmpty()) {
            resources.remove();
        }
        return removed;
    }
}

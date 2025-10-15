package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Map<DataSource, Connection> getResourceMap() {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        return map;
    }

    public static Connection getResource(DataSource key) {
        return getResourceMap().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getResourceMap().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Connection connection = getResourceMap().remove(key);
        if (getResourceMap().isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}
package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            Map<DataSource, Connection> connectionMap = new HashMap<>();
            connectionMap.put(key, value);
            resources.set(connectionMap);
            return;
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().remove(key);
    }
}

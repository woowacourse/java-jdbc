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
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            Map<DataSource, Connection> map = new HashMap<>();
            map.put(key, value);
            resources.set(map);
            return;
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}

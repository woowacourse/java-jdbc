package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static boolean hasResource(DataSource key) {
        if (resources.get() == null) {
            return false;
        }
        return resources.get().containsKey(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            throw new IllegalStateException("Cannot Unbind Uninitialized resource");
        }
        return resources.get().remove(key);
    }
}

package com.interface21.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
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

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> resource = new HashMap<>();
        resource.put(key, value);
        resources.set(resource);
    }

    public static void unbindResource(DataSource key) {
        resources.get().remove(key);
    }
}

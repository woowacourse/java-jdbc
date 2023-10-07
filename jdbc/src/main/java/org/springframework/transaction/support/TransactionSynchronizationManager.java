package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources;

    static {
        resources = new ThreadLocal<>();
        resources.set(new ConcurrentHashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }
}

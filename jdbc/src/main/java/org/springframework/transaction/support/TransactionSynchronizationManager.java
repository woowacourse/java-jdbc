package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(() -> new HashMap<>());

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getResources().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getResources().put(key, value);
    }

    public static void unbindResource(DataSource key) {
        getResources().remove(key);
    }

    private static Map<DataSource, Connection> getResources() {
        return resources.get();
    }

}

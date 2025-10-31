package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources =
            ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Map<DataSource, Connection> getResources() {
        return resources.get();
    }

    public static boolean hasResource(DataSource key) {
        return getResources().containsKey(key);
    }

    public static Connection getResource(DataSource key) {
        return getResources().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Objects.requireNonNull(value, "Connection must not be null");
        getResources().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getResources().remove(key);
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getCurrentThreadResource().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getCurrentThreadResource().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getCurrentThreadResource().remove(key);
    }

    private static Map<DataSource, Connection> getCurrentThreadResource() {
        return resources.get();
    }
}

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
        return getResourceMap().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getResourceMap().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return getResourceMap().remove(key);
    }

    private static Map<DataSource, Connection> getResourceMap() {
        return resources.get();
    }
}

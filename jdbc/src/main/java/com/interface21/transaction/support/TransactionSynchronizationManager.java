package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return RESOURCES.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        RESOURCES.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return RESOURCES.get().remove(key);
    }
}

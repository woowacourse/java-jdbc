package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<DataSource, Boolean>> TRANSACTION_ACTIVE = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        return RESOURCES.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        RESOURCES.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return RESOURCES.get().remove(key);
    }

    public static boolean isTransactionActive(DataSource key) {
        Boolean active = TRANSACTION_ACTIVE.get().get(key);
        if (active == null) {
            return false;
        }
        return active;
    }

    public static void setTransactionActive(DataSource key, boolean active) {
        TRANSACTION_ACTIVE.get().put(key, active);
    }
}

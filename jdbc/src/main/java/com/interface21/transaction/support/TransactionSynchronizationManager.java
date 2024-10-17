package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        return getConnectionMap().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        getConnectionMap().put(key, value);
    }

    @Nullable
    public static Connection unbindResource(DataSource key) {
        return getConnectionMap().remove(key);
    }

    private static Map<DataSource, Connection> getConnectionMap() {
        return resources.get();
    }
}

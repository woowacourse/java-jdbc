package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        return getThreadMap().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        getThreadMap().put(key,value);
    }

    public static Connection unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();
        if (isActiveTransaction()) {
            return connections.remove(key);
        }
        return null;
    }

    private static Map<DataSource, Connection> getThreadMap() {
        return resources.get();
    }

    public static boolean isActiveTransaction() {
        return resources.get() != null;
    }

    public static void clear() {
        resources.remove();
    }
}

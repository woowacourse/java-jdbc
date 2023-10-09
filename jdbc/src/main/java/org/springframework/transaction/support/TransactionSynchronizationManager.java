package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }

    public static boolean isTransactionActive(final DataSource dataSource) {
        return resources.get().containsKey(dataSource);
    }

    public static void clear() {
        resources.get().clear();
    }
}

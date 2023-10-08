package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.set(Map.of(key, value));
    }

    public static Connection unbindResource(DataSource key) {
        final Connection connection = resources.get().get(key);
        resources.remove();
        return connection;
    }
}

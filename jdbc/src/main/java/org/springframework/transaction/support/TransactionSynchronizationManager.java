package org.springframework.transaction.support;

import java.util.HashMap;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources;

    static {
        resources = new ThreadLocal<>();
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}

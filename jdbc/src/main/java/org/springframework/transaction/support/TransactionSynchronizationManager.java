package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionManager>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static ConnectionManager getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final ConnectionManager value) {
        resources.get().put(key, value);
    }

    public static ConnectionManager unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}

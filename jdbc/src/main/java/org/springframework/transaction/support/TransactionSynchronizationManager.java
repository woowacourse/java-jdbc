package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> connectionByDataSource = resources.get();
        if (Objects.isNull(connectionByDataSource)) {
            return null;
        }
        return connectionByDataSource.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        resources.set(new ConcurrentHashMap<>(
                Map.of(key, value)
        ));
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}

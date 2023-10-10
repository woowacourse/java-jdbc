package org.springframework.transaction.support;

import java.util.HashMap;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionEntry = resources.get();
        if (Objects.isNull(dataSourceConnectionEntry)) {
            return null;
        }
        return dataSourceConnectionEntry.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> dataSourceConnectionEntry = resources.get();
        if (Objects.isNull(dataSourceConnectionEntry)) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        return resources.get().remove(key);
    }

    public static void remove() {
        resources.remove();
    }
}

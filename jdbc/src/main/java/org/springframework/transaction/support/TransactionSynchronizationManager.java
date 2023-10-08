package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> connectionByDataSource = resources.get();
        if (Objects.isNull(connectionByDataSource)) {
            return null;
        }

        return connectionByDataSource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (Objects.isNull(resources.get())) {
            resources.set(new HashMap<>());
        }

        final Map<DataSource, Connection> mappings = resources.get();
        mappings.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> mappings = resources.get();
        return mappings.remove(key);
    }
}

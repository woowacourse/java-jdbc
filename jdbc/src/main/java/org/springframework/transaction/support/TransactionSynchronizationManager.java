package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        if (dataSourceConnectionMap == null) {
            return null;
        }

        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new HashMap<>();
        }

        dataSourceConnectionMap.put(key, value);
        resources.set(dataSourceConnectionMap);
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get().remove(key);
    }
}

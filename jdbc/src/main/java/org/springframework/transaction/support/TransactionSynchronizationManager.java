package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();

        if (dataSourceConnection == null) {
            return null;
        }

        return dataSourceConnection.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();

        if (dataSourceConnection == null) {
            dataSourceConnection = new HashMap<>();
            resources.set(dataSourceConnection);
        }

        dataSourceConnection.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        return resources.get()
                .remove(key);
    }
}

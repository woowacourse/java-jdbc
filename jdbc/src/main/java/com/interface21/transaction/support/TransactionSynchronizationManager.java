package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        if (dataSourceConnection.containsKey(key)) {
            return dataSourceConnection.get(key);
        }

        return null;
    }

    public static void bindResource(final DataSource key, final Connection value) {
        resources.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = resources.get();
        return dataSourceConnection.remove(key);
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    @Nullable
    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnMap = resources.get();
        if (dataSourceConnMap != null) {
            return dataSourceConnMap.get(key);
        }
        return null;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnMap = resources.get();
        if (dataSourceConnMap == null) {
            dataSourceConnMap = new HashMap<>();
            resources.set(dataSourceConnMap);
        }
        dataSourceConnMap.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnMap = resources.get();
        return dataSourceConnMap.remove(key);
    }
}

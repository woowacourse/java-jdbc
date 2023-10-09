package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        if (dataSourceConnectionMap == null) {
            return null;
        }
        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }

        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceConnectionMap = resources.get();

        if (dataSourceConnectionMap == null) {
            return null;
        }
        return dataSourceConnectionMap.remove(key);
    }

}

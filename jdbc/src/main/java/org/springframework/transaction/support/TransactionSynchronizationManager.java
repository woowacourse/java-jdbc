package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.ConnectionHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, ConnectionHolder>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, ConnectionHolder> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key).getConnection();
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.set(Map.of(key, new ConnectionHolder(value)));
    }

    public static void unbindResource(DataSource key) {
        ConnectionHolder connectionHolder = resources.get().get(key);
        if (connectionHolder.close()) {
            resources.remove();
        }
    }
}

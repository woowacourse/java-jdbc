package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Integer> count = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionMap = resources.get();
        if (connectionMap == null) {
            return null;
        }

        count.set(count.get() + 1);
        return connectionMap.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionMap = new HashMap<>();
        connectionMap.put(key, value);
        resources.set(connectionMap);

        count.set(1);
    }

    public static Connection unbindResource(DataSource key, Connection connection) throws SQLException {
        count.set(count.get() - 1);

        if (count.get() == 0) {
            connection.close();
            return resources.get().remove(key);
        }
        return resources.get().get(key);
    }
}

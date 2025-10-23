package com.interface21.transaction.support;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return;
        }

        Connection connection = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }

        if (connection != null) {
            DataSourceUtils.releaseConnection(connection, key);
        }
    }
}

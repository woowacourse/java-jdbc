package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        setIfNotContainsResources();
        Map<DataSource, Connection> dataSourceConnectionMapInThread = resources.get();
        if (dataSourceConnectionMapInThread.containsKey(key)) {
            return dataSourceConnectionMapInThread.get(key);
        }

        return dataSourceConnectionMapInThread.computeIfAbsent(key, dataSource -> {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        });
    }

    public static void bindResource(DataSource key, Connection value) {
        setIfNotContainsResources();
        Map<DataSource, Connection> dataSourceConnectionMapInThread = resources.get();
        dataSourceConnectionMapInThread.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMapInThread = resources.get();
        if (dataSourceConnectionMapInThread == null) {
            return null;
        }

        return dataSourceConnectionMapInThread.remove(key);
    }

    private static void setIfNotContainsResources() {
        Map<DataSource, Connection> dataSourceConnectionMapInThread = resources.get();
        if (dataSourceConnectionMapInThread == null) {
            resources.set(new HashMap<>());
        }
    }
}

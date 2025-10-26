package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnections = getDataSourceConnections();

        return dataSourceConnections.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnections = getDataSourceConnections();

        dataSourceConnections.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnections = getDataSourceConnections();

        Connection connection = dataSourceConnections.get(key);

        if (connection != null) {
            dataSourceConnections.remove(key);
        }

        return connection;
    }

    private static Map<DataSource, Connection> getDataSourceConnections() {
        Map<DataSource, Connection> dataSourceConnections = resources.get();

        if (dataSourceConnections == null) {
            dataSourceConnections = new HashMap<>();
            resources.set(dataSourceConnections);
        }
        return dataSourceConnections;
    }
}

package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final var dataSourceConnections = getDataSourceConnectionMap();
        return dataSourceConnections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final var dataSourceConnections = getDataSourceConnectionMap();
        dataSourceConnections.put(key, value);
    }

    public static Connection unbindResource(final DataSource key) throws SQLException {
        final var dataSourceConnections = getDataSourceConnectionMap();
        final var connection = dataSourceConnections.get(key);
        if (connection != null) {
            return dataSourceConnections.remove(key);
        }
        return connection;
    }

    private static Map<DataSource, Connection> getDataSourceConnectionMap() {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        if (dataSourceConnectionMap == null) {
            dataSourceConnectionMap = new HashMap<>();
            resources.set(dataSourceConnectionMap);
        }
        return dataSourceConnectionMap;
    }
}

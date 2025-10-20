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

    public static Connection getResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceToConnection = resources.get();
        if (dataSourceToConnection == null) {
            return null;
        }
        return dataSourceToConnection.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        final Map<DataSource, Connection> dataSourceToConnection = resources.get();
        if (dataSourceToConnection == null) {
            final Map<DataSource, Connection> createdDataSourceToConnection = new HashMap<>();
            createdDataSourceToConnection.put(key, value);
            resources.set(createdDataSourceToConnection);
            return;
        }
        dataSourceToConnection.put(key, value);
    }

    public static Connection unbindResource(DataSource key) throws SQLException {
        final Map<DataSource, Connection> dataSourceToConnection = resources.get();
        if (dataSourceToConnection == null) {
            throw new IllegalStateException("unbind 대상 resource 부재");
        }
        final Connection connection = dataSourceToConnection.remove(key);
        if (connection != null) {
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public static boolean hasConnection(final DataSource key) {
        final Map<DataSource, Connection> dataSourceToConnection = resources.get();
        if (dataSourceToConnection == null) {
            return false;
        }
        return dataSourceToConnection.containsKey(key);
    }
}

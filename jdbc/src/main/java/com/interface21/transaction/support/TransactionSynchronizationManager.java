package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationManager.class);
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

    public static Connection unbindResource(DataSource key) {
        final Map<DataSource, Connection> dataSourceToConnection = resources.get();
        if (dataSourceToConnection == null) {
            log.warn("unbind 대상 resource 부재");
            return null;
        }

        final Connection connection = dataSourceToConnection.remove(key);

        if (dataSourceToConnection.isEmpty()) {
            resources.remove();
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

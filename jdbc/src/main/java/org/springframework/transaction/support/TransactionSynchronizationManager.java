package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(ConcurrentHashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connectionsByDataSource = resources.get();
        return connectionsByDataSource.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connectionsByDataSource = resources.get();
        connectionsByDataSource.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connectionsByDataSource = resources.get();
        Connection connection = connectionsByDataSource.get(key);
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        connectionsByDataSource.remove(key);
        return connection;
    }
}

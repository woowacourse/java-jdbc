package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = getDataSourceConnection();
        return dataSourceConnection.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> dataSourceConnection = getDataSourceConnection();
        dataSourceConnection.put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> dataSourceConnection = getDataSourceConnection();
        return dataSourceConnection.remove(key);
    }

    private static Map<DataSource, Connection> getDataSourceConnection() {
        return resources.get();
    }
}

package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(() -> new HashMap());

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return getConnectionMap().get(key);
    }

    private static Map<DataSource, Connection> getConnectionMap() {
        return resources.get();
    }

    public static void bindResource(DataSource key, Connection value) {
        getConnectionMap().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        final Connection conn = getConnectionMap().remove(key);
        DataSourceUtils.releaseConnection(conn, key);
        return conn;
    }
}

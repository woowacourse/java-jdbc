package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    static {
        resources.set(new ConcurrentHashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        return dataSourceConnectionMap.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        dataSourceConnectionMap.put(key, value);
        resources.set(dataSourceConnectionMap);
    }

    public static Connection unbindResource(final DataSource key) {
        Map<DataSource, Connection> dataSourceConnectionMap = resources.get();
        Connection connection = dataSourceConnectionMap.get(key);
        try {
            boolean autoCommit = connection.getAutoCommit();
            if (!autoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return connection;
    }
}

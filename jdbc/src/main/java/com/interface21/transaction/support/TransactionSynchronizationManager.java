package com.interface21.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationManager.class);

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        final var map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        getResources().put(key, value);
        log.debug("Bound connection [{}] for datasource [{}]", value, key);
    }

    public static Connection unbindResource(final DataSource key) {
        final var value = getResources().remove(key);
        log.debug("Removed connection [{}] for datasource [{}]", value, key);
        return value;
    }

    public static boolean hasResource(final DataSource key) {
        return resources.get() != null && resources.get().containsKey(key);
    }

    private static Map<DataSource, Connection> getResources() {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        return resources.get();
    }

    public static Connection getConnection(final DataSource dataSource) throws SQLException {
        final var connection = getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        return dataSource.getConnection();
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (hasResource(dataSource)) {
            return;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                log.debug("Could not close JDBC Connection", e);
            }
        }
    }
}

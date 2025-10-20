package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(dataSource);
    }

    public static void bindConnection(final DataSource dataSource, final Connection connection) {
        Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(dataSource, connection);
    }

    public static Connection unbindConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        final Connection connection = map.remove(dataSource);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }

    public static boolean hasConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        return map != null && map.containsKey(dataSource);
    }

    public static boolean hasNotConnection(final DataSource dataSource) {
        return !hasConnection(dataSource);
    }
}

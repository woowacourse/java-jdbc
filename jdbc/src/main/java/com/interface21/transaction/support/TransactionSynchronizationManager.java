package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        return map.get(dataSource);
    }

    public static void bindConnection(final DataSource dataSource, final Connection connection) {
        Map<DataSource, Connection> map = resources.get();
        map.put(dataSource, connection);
    }

    public static Connection unbindConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        final Connection connection = map.remove(dataSource);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }

    public static boolean hasConnection(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        return map.containsKey(dataSource);
    }

    public static boolean hasNotConnection(final DataSource dataSource) {
        return !hasConnection(dataSource);
    }
}

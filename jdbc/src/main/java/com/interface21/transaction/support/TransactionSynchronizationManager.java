package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        return map.get(dataSource);
    }

    public static void bindResource(final DataSource dataSource, final Connection connection) {
        Map<DataSource, Connection> map = resources.get();
        map.put(dataSource, connection);
    }

    public static Connection unbindResource(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        final Connection connection = map.remove(dataSource);
        if (map.isEmpty()) {
            resources.remove();
        }
        return connection;
    }

    public static boolean hasResource(final DataSource dataSource) {
        final Map<DataSource, Connection> map = resources.get();
        return map.containsKey(dataSource);
    }

    public static boolean hasNotResource(final DataSource dataSource) {
        return !hasResource(dataSource);
    }
}

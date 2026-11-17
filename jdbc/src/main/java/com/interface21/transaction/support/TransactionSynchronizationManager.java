package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource dataSource) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(dataSource);
    }

    public static void bindResource(final DataSource dataSource, final Connection connection) {
        var connections = resources.get();
        if (connections == null) {
            connections = new IdentityHashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(dataSource, connection) != null) {
            throw new IllegalStateException("현재 스레드에 이 데이터소스의 연결이 이미 등록되어 있습니다.");
        }
    }

    public static Connection unbindResource(final DataSource dataSource) {
        final var connections = resources.get();
        if (connections == null) {
            return null;
        }

        final var connection = connections.remove(dataSource);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

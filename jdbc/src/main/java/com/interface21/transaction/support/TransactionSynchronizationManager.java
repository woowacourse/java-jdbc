package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, TransactionResource>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final var connections = resources.get();
        final var resource = connections == null ? null : connections.get(key);
        return resource == null ? null : resource.connection;
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);

        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, new TransactionResource(value)) != null) {
            throw new IllegalStateException("이미 Connection이 등록된 DataSource입니다.");
        }
    }

    public static Connection unbindResource(final DataSource key) {
        final var connections = resources.get();
        if (connections == null || !connections.containsKey(key)) {
            throw new IllegalStateException("등록된 Connection이 없습니다.");
        }
        final var connection = connections.remove(key);
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection.connection;
    }

    public static void setRollbackOnly(final DataSource key) {
        getRequiredResource(key).rollbackOnly = true;
    }

    public static boolean isRollbackOnly(final DataSource key) {
        return getRequiredResource(key).rollbackOnly;
    }

    private static TransactionResource getRequiredResource(final DataSource key) {
        final var connections = resources.get();
        final var resource = connections == null ? null : connections.get(key);
        if (resource == null) {
            throw new IllegalStateException("등록된 Connection이 없습니다.");
        }
        return resource;
    }

    private static class TransactionResource {

        private final Connection connection;
        private boolean rollbackOnly;

        private TransactionResource(final Connection connection) {
            this.connection = connection;
        }
    }
}

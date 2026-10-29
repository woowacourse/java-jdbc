package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private static final ThreadLocal<Set<DataSource>> rollbackOnly = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final var connections = resources.get();
        return connections == null ? null : connections.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        var connections = resources.get();
        if (connections == null) {
            connections = new HashMap<>();
            resources.set(connections);
        }
        if (connections.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to data source");
        }
    }

    public static Connection unbindResource(final DataSource key) {
        final var connections = resources.get();
        if (connections == null) {
            return null;
        }
        final var connection = connections.remove(key);
        final var marked = rollbackOnly.get();
        if (marked != null) {
            marked.remove(key);
            if (marked.isEmpty()) {
                rollbackOnly.remove();
            }
        }
        if (connections.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
    public static void setRollbackOnly(final DataSource key) {
        if (getResource(key) == null) {
            throw new IllegalStateException("No transaction bound to data source");
        }
        var marked = rollbackOnly.get();
        if (marked == null) {
            marked = new HashSet<>();
            rollbackOnly.set(marked);
        }
        marked.add(key);
    }

    public static boolean isRollbackOnly(final DataSource key) {
        final var marked = rollbackOnly.get();
        return marked != null && marked.contains(key);
    }
}

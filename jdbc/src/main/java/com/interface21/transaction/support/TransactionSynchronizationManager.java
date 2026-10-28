package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(final DataSource key) {
        final var current = resources.get();
        return current == null ? null : current.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        if (getResource(key) != null) {
            throw new IllegalStateException("Connection already bound to this DataSource");
        }
        final var current = resources.get();
        final var updated = current == null
                ? new HashMap<DataSource, Connection>()
                : new HashMap<>(current);
        updated.put(key, value);
        resources.set(Map.copyOf(updated));
    }

    public static Connection unbindResource(final DataSource key) {
        final var connection = getResource(key);
        if (connection == null) {
            throw new IllegalStateException("No Connection bound to this DataSource");
        }
        final var updated = new HashMap<>(resources.get());
        updated.remove(key);
        if (updated.isEmpty()) {
            resources.remove();
        } else {
            resources.set(Map.copyOf(updated));
        }
        return connection;
    }
}

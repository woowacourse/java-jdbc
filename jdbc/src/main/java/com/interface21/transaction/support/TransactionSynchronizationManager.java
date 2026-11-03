package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var current = resources.get();
        return current == null ? null : current.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var current = resources.get();
        if (current == null) {
            current = new HashMap<>();
            resources.set(current);
        }
        if (current.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final var current = resources.get();
        if (current == null) {
            return null;
        }
        final var connection = current.remove(key);
        if (current.isEmpty()) {
            resources.remove();
        }
        return connection;
    }
}

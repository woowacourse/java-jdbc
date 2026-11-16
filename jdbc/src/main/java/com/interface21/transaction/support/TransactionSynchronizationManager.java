package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var bound = resources.get();
        if (bound == null) {
            return null;
        }
        return bound.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var bound = resources.get();
        if (bound == null) {
            bound = new HashMap<>();
            resources.set(bound);
        }
        if (bound.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Already a connection bound to the current thread for " + key);
        }
    }

    public static Connection unbindResource(DataSource key) {
        final var bound = resources.get();
        if (bound == null) {
            return null;
        }
        final var removed = bound.remove(key);
        if (bound.isEmpty()) {
            resources.remove();
        }
        return removed;
    }
}

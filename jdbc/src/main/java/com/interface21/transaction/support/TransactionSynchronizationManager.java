package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Set<DataSource>> rollbackOnlyResources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        final var boundResources = resources.get();
        return boundResources == null ? null : boundResources.get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        var boundResources = resources.get();
        if (boundResources == null) {
            boundResources = new HashMap<>();
            resources.set(boundResources);
        }
        if (boundResources.putIfAbsent(key, value) != null) {
            throw new IllegalStateException("Connection already bound to DataSource");
        }
    }

    public static Connection unbindResource(DataSource key) {
        final var boundResources = resources.get();
        if (boundResources == null) {
            return null;
        }
        final var connection = boundResources.remove(key);
        if (boundResources.isEmpty()) {
            resources.remove();
        }
        clearRollbackOnly(key);
        return connection;
    }

    public static void setRollbackOnly(DataSource key) {
        if (getResource(key) == null) {
            throw new IllegalStateException("No Connection bound to DataSource");
        }
        var rollbackOnly = rollbackOnlyResources.get();
        if (rollbackOnly == null) {
            rollbackOnly = new HashSet<>();
            rollbackOnlyResources.set(rollbackOnly);
        }
        rollbackOnly.add(key);
    }

    public static boolean isRollbackOnly(DataSource key) {
        final var rollbackOnly = rollbackOnlyResources.get();
        return rollbackOnly != null && rollbackOnly.contains(key);
    }

    private static void clearRollbackOnly(DataSource key) {
        final var rollbackOnly = rollbackOnlyResources.get();
        if (rollbackOnly == null) {
            return;
        }
        rollbackOnly.remove(key);
        if (rollbackOnly.isEmpty()) {
            rollbackOnlyResources.remove();
        }
    }
}

package org.springframework.transaction.support;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> actualTransactionActive = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TransactionSynchronizationManager() {}

    @Nullable
    public static Connection getResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            final Map<DataSource, Connection> newMap = new HashMap<>();
            newMap.put(key, value);
            resources.set(newMap);
            return;
        }
        map.put(key, value);
        resources.set(map);
    }

    public static void unbindResource(final DataSource key) {
        final Map<DataSource, Connection> map = resources.get();
        if (map == null) {
            throw new IllegalStateException("No resource for key [" + key + "] bound to thread");
        }
        if (map.get(key) == null) {
            throw new IllegalStateException("No value for key [" + key + "] bound to thread");
        }
        map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
    }

    public static boolean isActualTransactionActive() {
        return actualTransactionActive.get();
    }

    public static void setActualTransactionActiveTrue() {
        actualTransactionActive.set(true);
    }
}

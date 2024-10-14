package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCES = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return RESOURCES.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        RESOURCES.get().put(key, value);
    }

    public static Connection unbindResource(final DataSource key) {
        return RESOURCES.get().remove(key);
    }

    public static void unload() {
        RESOURCES.remove();
    }
}

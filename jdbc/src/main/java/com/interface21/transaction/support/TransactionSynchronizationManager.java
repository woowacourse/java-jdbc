package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
    private static final ThreadLocal<Integer> transactionCount = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> rollbackOnly = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            return null;
        }
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        if (resources.get() == null) {
            throw new IllegalStateException("No resource bound for key: " + key);
        }
        return resources.get().remove(key);
    }

    public static Integer getTransactionCount() {
        return transactionCount.get();
    }

    public static void setTransactionCount(Integer count) {
        transactionCount.set(count);
    }

    public static void removeTransactionCount() {
        transactionCount.remove();
    }

    public static Boolean isRollbackOnly() {
        return rollbackOnly.get();
    }

    public static void setRollbackOnly(Boolean rollback) {
        rollbackOnly.set(rollback);
    }

    public static void removeRollbackOnly() {
        rollbackOnly.remove();
    }
}

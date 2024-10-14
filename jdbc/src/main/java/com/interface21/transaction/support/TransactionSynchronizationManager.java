package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        if (isActualTransactionActive()) {
            return connections.get(key);
        }

        return null;
    }

    public static boolean isActualTransactionActive() {
        Map<DataSource, Connection> connections = resources.get();

        return connections != null;
    }

    public static void bindResource(DataSource key, Connection value) {
        resources.set(Map.of(key, value));
    }

    public static Object unbindResource(DataSource key) {
        resources.remove();
        return key;
    }
}

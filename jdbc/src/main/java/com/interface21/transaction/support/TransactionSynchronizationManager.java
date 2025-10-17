package com.interface21.transaction.support;

import java.sql.Connection;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Connection> resource = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getConnection() {
        return resource.get();
    }

    public static void bindConnection(final Connection connection) {
        resource.set(connection);
    }

    public static void unbindConnection() {
        resource.remove();
    }

    public static boolean hasConnection() {
        return resource.get() != null;
    }
}

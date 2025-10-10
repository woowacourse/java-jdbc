package com.interface21.jdbc.core;

import java.sql.Connection;

public class TransactionSynchronizationManager {

    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public static void setConnection(final Connection connection) {
        if (!hasConnection()) {
            connectionThreadLocal.set(connection);
        }
    }

    private static boolean hasConnection() {
        return connectionThreadLocal.get() != null;
    }

    public static Connection getConnection() {
        return connectionThreadLocal.get();
    }

    public static void closeConnection() {
        connectionThreadLocal.remove();
    }
}

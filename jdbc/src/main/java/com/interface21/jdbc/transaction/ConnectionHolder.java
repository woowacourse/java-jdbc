package com.interface21.jdbc.transaction;

import java.sql.Connection;

public class ConnectionHolder {

    private static final ThreadLocal<Connection> context = new ThreadLocal<>();

    public static void setConnection(Connection connection) {
        context.set(connection);
    }

    public static Connection getConnection() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}

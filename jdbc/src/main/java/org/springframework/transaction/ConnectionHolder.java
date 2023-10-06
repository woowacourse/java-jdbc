package org.springframework.transaction;

import java.sql.Connection;

import javax.annotation.Nullable;

public class ConnectionHolder {
    public static final ThreadLocal<Connection> HOLDER = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    @Nullable
    public static Connection getConnection() {
        return HOLDER.get();
    }

    public static void setConnection(final Connection connection) {
        HOLDER.set(connection);
    }

    public static void clean() {
        HOLDER.remove();
    }
}

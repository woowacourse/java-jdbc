package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

public class ConnectionHolder {
    public static final ThreadLocal<Connection> HOLDER = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    public static Connection getConnection() {
        final Connection connection = HOLDER.get();
        validateClose(connection);
        return connection;
    }

    private static void validateClose(final Connection connection) {
        try {
            if (connection.isClosed()) {
                throw new DataAccessException();
            }
        } catch(SQLException exception) {
            throw new DataAccessException();
        }
    }

    public static void setConnection(final Connection connection) {
        validateClose(connection);
        HOLDER.set(connection);
    }

    public static void clean() {
        HOLDER.remove();
    }

    public static boolean hasSame(final Connection connection) {
        return HOLDER.get() == connection;
    }
}

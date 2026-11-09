package com.interface21.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionManager {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            conn = dataSource.getConnection();
            connectionHolder.set(conn);
        }
        return conn;
    }

    public static void releaseConnection(Connection conn, DataSource dataSource) {
        if (conn == null) return;

        if (connectionHolder.get() == conn) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bindConnection(Connection conn) {
        connectionHolder.set(conn);
    }

    public static void unbindConnection() {
        connectionHolder.remove();
    }
}

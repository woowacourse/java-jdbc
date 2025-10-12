package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionManager {

    public static Connection startTransaction(final DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    public static void commitTransaction(final Connection connection) throws SQLException {
        connection.commit();
        connection.close();
    }

    public static void rollbackTransaction(final Connection connection) {
        try {
            connection.rollback();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

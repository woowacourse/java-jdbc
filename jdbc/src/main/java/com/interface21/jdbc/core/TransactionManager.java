package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private TransactionManager() {

    }

    public static void start(Connection connection, Runnable runnable) {
        try {
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private static void rollback(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}

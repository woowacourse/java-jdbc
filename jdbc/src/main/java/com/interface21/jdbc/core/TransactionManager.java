package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.dao.DataAccessException;

public class TransactionManager {

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
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    public void start(Connection connection, Runnable runnable) {
        try {
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}

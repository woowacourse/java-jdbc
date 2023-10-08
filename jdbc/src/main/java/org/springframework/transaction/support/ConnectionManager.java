package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectionManager {

    private ConnectionManager() {
    }

    public static void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException closeException) {
            throw new DataAccessException("failed to close connection", closeException);
        }
    }

    public static void rollback(Exception e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            throw new DataAccessException("failed to rollback transaction", rollbackException);
        }
        throw new DataAccessException(e);
    }
}

package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate <T> {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public T execute(ServiceCallback<T> serviceCallback) {
        Connection connection = null;
        T result = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setConnection(connection);

            result = serviceCallback.execute();

            connection.commit();
        } catch (Exception e) {
            rollback(e, connection);
        } finally {
            closeConnection(connection);
        }
        return result;
    }

    private static void rollback(Exception e, Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {}
        }
        throw new DataAccessException(e);
    }

    private static void closeConnection(Connection connection) {
        TransactionSynchronizationManager.closeConnection();
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}

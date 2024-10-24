package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    public static void transaction(DataSource dataSource, Runnable runnable) {
        Connection connection = getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Throwable e) {
            rollback(connection);
        } finally {
            releaseConnection(dataSource, connection);
        }
    }

    private static Connection getConnection(DataSource dataSource) {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            throw new DataAccessException("Failed to obtain JDBC Connection");
        }

        return connection;
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback", e);
        }
    }

    private static void releaseConnection(DataSource dataSource, Connection connection) {
        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close JDBC Connection", e);
        }
    }
}

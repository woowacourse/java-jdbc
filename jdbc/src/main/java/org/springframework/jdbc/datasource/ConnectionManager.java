package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionManager;

public class ConnectionManager {

    private static final ThreadLocal<Connection> connections = new ThreadLocal<>();

    private ConnectionManager() {
    }

    public static Connection getConnection(DataSource dataSource) {
        Connection connection = connections.get();
        if (connection != null) {
            return connection;
        }
        return initializeConnection(dataSource);
    }

    private static Connection initializeConnection(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            if (TransactionManager.isTransactionEnable()) {
                connection.setAutoCommit(false);
            }
            connections.set(connection);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public static boolean isConnectionEnable() {
        return connections.get() != null;
    }

    public static void releaseConnection() {
        Connection connection = connections.get();
        try {
            if (connection == null) {
                return;
            }
            closeConnection(connection);
        } finally {
            TransactionManager.clear();
        }
    }

    private static void closeConnection(Connection connection) {
        if (TransactionManager.isTransactionEnable()) {
            if (TransactionManager.isRollbackEnable()) {
                rollback(connection);
            } else {
                commit(connection);
            }
        }
        close(connection);
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
            // ignored
        }
        connections.remove();
    }
}

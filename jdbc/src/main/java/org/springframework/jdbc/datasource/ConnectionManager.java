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
        validateConnection(connection);
        if (TransactionManager.isTransactionEnable()) {
            if (TransactionManager.isRollbackEnable()) {
                rollback(connection);
            } else {
                commit(connection);
            }
            TransactionManager.clear();
        }
        close(connection);
    }

    private static void validateConnection(Connection connection) {
        // IntelliJ 에서 Condition 'connection == null' is always 'false' 발생하는데, 버그 같음.
        if (connection == null) {
            throw new IllegalStateException("Connection is not enabled!");
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // ignored
        }
    }

    private static void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException ignored) {
            // ignored
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

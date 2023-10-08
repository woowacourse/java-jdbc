package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private static final ThreadLocal<Connection> connection = new ThreadLocal<>();

    private ConnectionManager() {
    }

    public static Connection getConnection(final DataSource dataSource) {
        final Connection connection = ConnectionManager.connection.get();
        if (connection != null) {
            return connection;
        }
        return initializeConnection(dataSource);
    }

    private static Connection initializeConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();
            if (TransactionManager.isTransactionEnable()) {
                connection.setAutoCommit(false);
            }
            ConnectionManager.connection.set(connection);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public static boolean isConnectionEnable() {
        return connection.get() != null;
    }

    public static void releaseConnection() {
        final Connection connection = ConnectionManager.connection.get();
        if (connection == null) {
            return;
        }
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
        ConnectionManager.connection.remove();
    }
}

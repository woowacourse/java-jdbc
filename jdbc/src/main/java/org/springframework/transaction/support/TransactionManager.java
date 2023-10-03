package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private static final ThreadLocal<Connection> connections = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> transactionEnables = new ThreadLocal<>();

    private TransactionManager() {
    }

    public static void begin() {
        transactionEnables.set(Boolean.TRUE);
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
            if (isTransactionEnable()) {
                connection.setAutoCommit(false);
            }
            connections.set(connection);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public static boolean isTransactionEnable() {
        Boolean isTransaction = transactionEnables.get();
        if (isTransaction == null) {
            return false;
        }
        return isTransaction;
    }

    public static boolean isConnectionEnable() {
        return connections.get() != null;
    }

    public static void commit() {
        validateTransactionEnable();
        Connection connection = connections.get();
        validateConnection(connection);
        commit(connection);
    }

    private static void validateTransactionEnable() {
        if (!isTransactionEnable()) {
            throw new IllegalStateException("Transaction is not enabled!");
        }
    }

    private static void validateConnection(Connection connection) {
        // IntelliJ 에서 Condition 'connection == null' is always 'false' 발생하는데, 버그 같음.
        if (connection == null) {
            throw new IllegalStateException("Connection is not enabled!");
        }
    }

    private static void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException ignored) {
            // ignored
        }
        transactionEnables.remove();
    }

    public static void rollback() {
        validateTransactionEnable();
        Connection connection = connections.get();
        validateConnection(connection);
        rollback(connection);
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // ignored
        }
        transactionEnables.remove();
    }

    public static void releaseConnection() {
        Connection connection = connections.get();
        validateConnection(connection);
        if (isTransactionEnable()) {
            rollback(connection);
        }
        close(connection);
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

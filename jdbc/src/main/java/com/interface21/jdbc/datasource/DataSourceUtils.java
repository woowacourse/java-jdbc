package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    /**
     * Returns the connection bound to the current transaction, or a new connection that the caller must release.
     * A new connection is not bound here; only the transaction owner binds and unbinds it.
     */
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    /** Closes the connection unless it belongs to the current transaction, whose owner closes it. */
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null || isTransactional(connection, dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }

    /**
     * Releases the connection after an operation that may have failed.
     * If the operation failed, a close failure is attached to that failure instead of replacing it.
     */
    public static void releaseConnection(Connection connection, DataSource dataSource, Throwable primaryFailure) {
        if (primaryFailure == null) {
            releaseConnection(connection, dataSource);
            return;
        }
        if (connection == null || isTransactional(connection, dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException | RuntimeException closeFailure) {
            primaryFailure.addSuppressed(closeFailure);
        }
    }
    public static boolean isTransactional(Connection connection, DataSource dataSource) {
        return TransactionSynchronizationManager.getResource(dataSource) == connection;
    }
}
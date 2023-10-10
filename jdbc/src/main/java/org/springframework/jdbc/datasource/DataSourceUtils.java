package org.springframework.jdbc.datasource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseJdbcConnection(Connection connection, DataSource dataSource) {
        try {
            if (isTransactionNotStarted(connection)) {
                releaseConnection(connection, dataSource);
            }
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }

    private static boolean isTransactionNotStarted(final Connection connection) throws SQLException {
        return connection.getAutoCommit() == true;
    }

    public static void releaseTransactionConnection(Connection connection, DataSource dataSource) {
        try {
            connection.setAutoCommit(true);
            releaseConnection(connection, dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close Connection");
        }
    }

    private static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close Connection");
        }
    }
}

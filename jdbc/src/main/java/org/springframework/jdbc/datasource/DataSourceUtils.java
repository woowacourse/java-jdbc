package org.springframework.jdbc.datasource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) {
        if (TransactionSynchronizationManager.isInTransaction()) {
            return TransactionSynchronizationManager.getConnection(dataSource);
        }
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    public static void releaseConnection(Connection connection) {
        try {
            releaseIfNotInTransaction(connection);
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }

    private static void releaseIfNotInTransaction(Connection connection) throws SQLException {
        if (!TransactionSynchronizationManager.isInTransaction()) {
            connection.close();
        }
    }
}

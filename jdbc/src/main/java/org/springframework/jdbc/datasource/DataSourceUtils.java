package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.exception.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

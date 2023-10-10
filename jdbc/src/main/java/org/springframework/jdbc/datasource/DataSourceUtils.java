package org.springframework.jdbc.datasource;

import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        return generateConnection(dataSource);
    }

    private static Connection generateConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final DataSource dataSource) {
        try {
            Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

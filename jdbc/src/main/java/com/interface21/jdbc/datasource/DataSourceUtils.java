package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotCloseJdbcConnectionException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            final Connection newConnection = dataSource.getConnection();
            return newConnection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource)
            throws CannotCloseJdbcConnectionException {
        if ((connection == null) || (connection == TransactionSynchronizationManager.getResource(dataSource))) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotCloseJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

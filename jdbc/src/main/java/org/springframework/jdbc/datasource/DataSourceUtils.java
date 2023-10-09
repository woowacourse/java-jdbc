package org.springframework.jdbc.datasource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null) {
            return connectionHolder.getConnection();
        }

        try {
            final Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        final ConnectionHolder currentConnectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (currentConnectionHolder.isTransactionActive()) {
            return;
        }

        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

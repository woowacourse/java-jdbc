package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {

        if (connection == null) {
            return;
        }

        if (connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", e);
        }
    }
}

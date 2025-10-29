package com.interface21.jdbc.datasource;

import com.interface21.jdbc.exception.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final var connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection != null) {
            return connection;
        }

        try {
            final var newConnection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, newConnection);
            return newConnection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection conn, final DataSource dataSource) {
        final var connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection == conn) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

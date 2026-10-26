package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) {
        final var boundConnection =
                TransactionSynchronizationManager.getResource(dataSource);

        if (boundConnection != null) {
            return boundConnection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException(
                    "Failed to obtain JDBC Connection", e
            );
        }
    }

    public static void releaseConnection(
            Connection connection, DataSource dataSource) {

        if (connection == null ||
                connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException(
                    "Failed to close JDBC Connection", e
            );
        }
    }
}

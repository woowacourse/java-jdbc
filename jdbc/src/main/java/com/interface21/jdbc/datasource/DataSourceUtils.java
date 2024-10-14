package com.interface21.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

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

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {

            TransactionSynchronizationManager.unbindResource(dataSource);
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

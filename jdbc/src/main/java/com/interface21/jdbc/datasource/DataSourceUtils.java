package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource, Throwable failure) {
        try {
            releaseConnection(connection, dataSource);
        } catch (RuntimeException | Error e) {
            if (failure == null) {
                throw e;
            }
            failure.addSuppressed(e);
        }
    }
}

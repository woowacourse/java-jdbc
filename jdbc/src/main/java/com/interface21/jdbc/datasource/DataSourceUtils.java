package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

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
        if (connection == null || isConnectionTransactional(connection, dataSource)) {
            return;
        }

        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }

    private static boolean isConnectionTransactional(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return false;
        }

        try {
            return !connection.isClosed() && connection.getMetaData().getURL().equals(dataSource.getConnection().getMetaData().getURL());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to determine if the connection is transactional", e);
        }
    }
}

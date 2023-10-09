package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            if (isOpen(connection)) {
                return connection;
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
            Connection newConnection = newConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource, newConnection);
            return newConnection;
        }

        connection = newConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        return connection;
    }

    private static boolean isOpen(Connection connection) {
        try {
            return !connection.isClosed();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static Connection newConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

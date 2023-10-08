package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        return getNewConnection(dataSource);
    }

    private static Connection getNewConnection(DataSource dataSource) {
        try {
            Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            if (!connection.getAutoCommit()) {
                return;
            }

            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

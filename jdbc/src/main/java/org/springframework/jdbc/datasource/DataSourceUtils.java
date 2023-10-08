package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        return createConnection(dataSource);
    }

    private static Connection createConnection(final DataSource dataSource) {
        try {
            final Connection connection = dataSource.getConnection();
            if (TransactionManager.isTxBegin(dataSource)) {
                connection.setAutoCommit(false);
                TransactionSynchronizationManager.bindResource(dataSource, connection);
            }
            return connection;

        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (TransactionManager.isTxBegin(dataSource)) {
            return;
        }
        try {
            connection.close();

        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

package org.springframework.jdbc.datasource;

import org.springframework.jdbc.exception.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null) {
            return connectionHolder.getConnection();
        }

        try {
            Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if(connection == null) {
            return;
        }
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        boolean isConnectionActive = connectionHolder.isConnectionTransactionActive();
        if(isConnectionActive) {
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

package org.springframework.jdbc.datasource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null) {
            return connectionHolder.getConnection();
        }
        try {
            Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, new ConnectionHolder(connection));
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("");
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder.isTransaction()) {
            return;
        }
        try {
            ConnectionHolder unbindConnection = TransactionSynchronizationManager.unbindResource(dataSource);
            if (unbindConnection.isSameConnection(connection)) {
                connection.close();
            }
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("");
        }
    }
}

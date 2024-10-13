package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.ConnectionHolder;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null) {
            return connectionHolder.getConnection();
        }

        try {
            Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder.isTransactionActive()) {
            return;
        }
        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

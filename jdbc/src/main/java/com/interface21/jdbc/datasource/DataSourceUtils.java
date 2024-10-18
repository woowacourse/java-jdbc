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
        if (!TransactionSynchronizationManager.isTransactionActive()) {
            return getConnectionFromDataSource(dataSource);
        }

        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            connection = bindConnection(dataSource);
        }

        return connection;
    }

    private static Connection bindConnection(DataSource dataSource) {
        Connection connection = getConnectionFromDataSource(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        return connection;
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }

    private static Connection getConnectionFromDataSource(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }
}

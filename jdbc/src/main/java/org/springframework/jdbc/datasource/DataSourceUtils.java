package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.SimpleConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final SimpleConnectionHolder simpleConnectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (simpleConnectionHolder != null) {
            return simpleConnectionHolder.getConnection();
        }

        try {
            final Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, new SimpleConnectionHolder(connection));
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

package org.springframework.jdbc.datasource;

import static java.util.Objects.isNull;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null) {
            return connectionHolder.getConnection();
        }

        try {
            final Connection connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void startTransaction(final Connection connection, final DataSource dataSource) {
        final ConnectionHolder connectionHolder = getConnectionHolder(connection, dataSource);
        try{
            connectionHolder.setTransactionActive(true);
            connection.setAutoCommit(false);
        }catch(SQLException e) {
            throw new DataAccessException();
        }
    }

    private static ConnectionHolder getConnectionHolder(final Connection connection, final DataSource dataSource) {
        final ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if(isNull(connectionHolder)) {
            throw new IllegalStateException();
        }
        if(!connectionHolder.has(connection)) {
            throw new IllegalStateException();
        }
        return connectionHolder;
    }

    public static void finishTransaction(final Connection connection, final DataSource dataSource) {
        final ConnectionHolder connectionHolder = getConnectionHolder(connection, dataSource);
        try{
            connection.commit();
            connectionHolder.setTransactionActive(false);
        }catch(SQLException e) {
            throw new DataAccessException();
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        try {
            final ConnectionHolder connectionHolder = getConnectionHolder(connection, dataSource);
            if(connectionHolder.isTransactionActive()) {
                return;
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

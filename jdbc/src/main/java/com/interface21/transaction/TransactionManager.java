package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.ConnectionHolder;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T runInTransaction(Supplier<T> supplier) {
        try {
            return doInTransaction(supplier);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public void runInTransaction(Runnable runnable) {
        try {
            doInTransaction(() -> {
                runnable.run();
                return null;
            });
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    private <T> T doInTransaction(Supplier<T> supplier) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            activeTransaction(connection);
            T result = supplier.get();
            operateConnection(Connection::commit);
            return result;
        } catch (Exception exception) {
            operateConnection(Connection::rollback);
            throw new DataAccessException(exception);
        } finally {
            releaseConnection(connection);
        }
    }

    private void activeTransaction(Connection connection) throws SQLException {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        connectionHolder.setTransactionActive(true);
        connection.setAutoCommit(false);
    }

    private void operateConnection(ConnectionCallback callback) throws SQLException {
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        if (connectionHolder != null && connectionHolder.isTransactionActive()) {
            callback.doInConnection(connectionHolder.getConnection());
            connectionHolder.setTransactionActive(false);
        }
    }

    private void releaseConnection(Connection connection) throws SQLException {
        connection.setAutoCommit(true);
        ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
        connectionHolder.setTransactionActive(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}

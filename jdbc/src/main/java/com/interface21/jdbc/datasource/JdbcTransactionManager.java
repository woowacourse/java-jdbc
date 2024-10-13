package com.interface21.jdbc.datasource;

import com.interface21.transaction.PlatformTransactionManager;
import com.interface21.transaction.TransactionException;
import com.interface21.transaction.support.TransactionHolder;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcTransactionManager implements PlatformTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TransactionHolder startTransaction() {
        TransactionSynchronizationManager.initTransactionActive();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionHolder transactionHolder = new TransactionHolder(connection);
        try {
            switchManualCommit(transactionHolder);
        } catch (SQLException e) {
            throw new TransactionException("Database access error occurred", e);
        }
        return transactionHolder;
    }

    private void switchManualCommit(TransactionHolder transactionHolder) throws SQLException {
        Connection connection = (Connection) transactionHolder.getTransaction();
        if (connection.getAutoCommit()) {
            transactionHolder.setMustRestoreAutoCommit(true);
            connection.setAutoCommit(false);
        }
    }

    @Override
    public void commit(TransactionHolder transactionHolder) {
        Connection connection = (Connection) transactionHolder.getTransaction();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new TransactionException("JDBC commit failed", e);
        } finally {
            cleanupAfterTransaction(transactionHolder);
        }
    }

    @Override
    public void rollback(TransactionHolder transactionHolder) {
        Connection connection = (Connection) transactionHolder.getTransaction();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException("JDBC rollback failed", e);
        } finally {
            cleanupAfterTransaction(transactionHolder);
        }
    }

    private void cleanupAfterTransaction(TransactionHolder transactionHolder) {
        TransactionSynchronizationManager.clear();
        TransactionSynchronizationManager.unbindResource(this.dataSource);
        Connection connection = (Connection) transactionHolder.getTransaction();
        if (transactionHolder.isMustRestoreAutoCommit()) {
            resetConnection(connection);
        }
        DataSourceUtils.releaseConnection(connection, this.dataSource);
    }

    private void resetConnection(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionException("JDBC Connection reset failed after transaction", e);
        }
    }
}

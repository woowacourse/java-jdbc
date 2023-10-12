package com.techcourse.service.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithTransaction(final TransactionCommandExecutor transactionCommandExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            transactionCommandExecutor.run();

            connection.commit();
        } catch (final SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            release(dataSource, connection);
        }
    }

    private void rollback(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    private static void release(final DataSource dataSource, final Connection connection) {
        try {
            connection.setAutoCommit(true);
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (final SQLException e) {
            throw new TransactionException("Failed to change auto commit to true");
        }
    }

    public <T> T queryWithTransaction(final TransactionQueryExecutor<T> transactionQueryExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);

            final T result = transactionQueryExecutor.get();

            connection.commit();
            return result;
        } catch (final SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            release(dataSource, connection);
        }
    }
}

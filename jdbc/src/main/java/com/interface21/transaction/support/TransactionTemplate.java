package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        final boolean isNewTransaction = !DataSourceUtils.hasResource(dataSource);
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            if (shouldSkipTransactionManagement(connection, isNewTransaction)) {
                runnable.run();
                return;
            }

            configureConnectionForTransaction(connection);
            runnable.run();
            commitIfNewTransaction(connection, isNewTransaction);
        } catch (final Exception e) {
            handleTransactionException(connection, isNewTransaction, e);
        } finally {
            cleanupTransaction(connection, isNewTransaction);
        }
    }

    private boolean shouldSkipTransactionManagement(final Connection connection, final boolean isNewTransaction) {
        if (isNewTransaction) {
            return false;
        }

        final boolean isInAutoCommitMode = isAutoCommitEnabled(connection);
        return !isInAutoCommitMode;
    }

    private void configureConnectionForTransaction(final Connection connection) {
        if (isAutoCommitEnabled(connection)) {
            try {
                connection.setAutoCommit(false);
            } catch (final SQLException e) {
                throw new DataAccessException("Failed to disable auto-commit", e);
            }
        }
    }

    private void commitIfNewTransaction(final Connection connection, final boolean isNewTransaction) {
        if (isNewTransaction) {
            try {
                connection.commit();
            } catch (final SQLException e) {
                throw new DataAccessException("Failed to commit transaction", e);
            }
        }
    }

    private void handleTransactionException(final Connection connection, final boolean isNewTransaction, final Exception exception) {
        if (isNewTransaction) {
            rollback(connection, exception);
        }

        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }
        throw new DataAccessException(exception);
    }

    private void cleanupTransaction(final Connection connection, final boolean isNewTransaction) {
        if (isNewTransaction) {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private boolean isAutoCommitEnabled(final Connection connection) {
        try {
            return connection.getAutoCommit();
        } catch (final SQLException e) {
            throw new DataAccessException("Failed to check auto-commit status", e);
        }
    }

    private void rollback(final Connection connection, final Exception originalException) {
        try {
            connection.rollback();
        } catch (final SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}

package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public <T> T execute(final TransactionCallback<T> callback) throws Throwable {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            try {
                return callback.execute();
            } catch (Throwable failure) {
                TransactionSynchronizationManager.setRollbackOnly(dataSource);
                throw failure;
            }
        }
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        boolean bound = false;
        boolean started = false;
        boolean canRestore = true;
        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;
            final T result = callback.execute();
            if (TransactionSynchronizationManager.isRollbackOnly(dataSource)) {
                throw new DataAccessException("Transaction marked rollback-only by a participating operation");
            }
            connection.commit();
            return result;
        } catch (Throwable exception) {
            failure = exception instanceof SQLException
                    ? new DataAccessException("Failed to complete transaction", exception) : exception;
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    canRestore = false;
                    failure.addSuppressed(rollbackFailure);
                }
            }
            throw failure;
        } finally {
            if (bound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            Throwable cleanupFailure = null;
            try {
                if (started && canRestore) {
                    connection.setAutoCommit(autoCommit);
                }
            } catch (SQLException exception) {
                cleanupFailure = new DataAccessException("Failed to restore auto-commit", exception);
            }
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (RuntimeException exception) {
                if (cleanupFailure == null) {
                    cleanupFailure = exception;
                } else {
                    cleanupFailure.addSuppressed(exception);
                }
            }
            if (cleanupFailure != null) {
                if (failure != null) {
                    failure.addSuppressed(cleanupFailure);
                } else {
                    throw cleanupFailure;
                }
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute() throws Throwable;
    }
}

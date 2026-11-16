package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithoutResult(final Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    public <T> T execute(final Supplier<T> action) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }

        final var connection = DataSourceUtils.getConnection(dataSource);
        boolean bound = false;
        Throwable failure = null;
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;
            return executeAndCommit(connection, action);
        } catch (SQLException exception) {
            final var translated = new DataAccessException(exception);
            failure = translated;
            throw translated;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            throw exception;
        } finally {
            cleanup(connection, bound, failure);
        }
    }

    private <T> T executeAndCommit(final Connection connection, final Supplier<T> action) throws SQLException {
        try {
            final var result = action.get();
            connection.commit();
            return result;
        } catch (SQLException | RuntimeException | Error exception) {
            try {
                connection.rollback();
            } catch (SQLException | RuntimeException | Error rollbackException) {
                if (exception != rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
            }
            throw exception;
        }
    }

    private void cleanup(final Connection connection, final boolean bound, Throwable failure) {
        try {
            if (bound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (RuntimeException | Error unbindException) {
            if (failure == null) {
                failure = unbindException;
                throw unbindException;
            }
            if (failure != unbindException) {
                failure.addSuppressed(unbindException);
            }
        } finally {
            releaseConnection(connection, failure);
        }
    }

    private void releaseConnection(final Connection connection, final Throwable failure) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (RuntimeException | Error closeException) {
            if (failure != null) {
                if (failure != closeException) {
                    failure.addSuppressed(closeException);
                }
            } else if (closeException instanceof Error error) {
                throw error;
            } else {
                log.warn("Failed to close JDBC Connection after commit", closeException);
            }
        }
    }
}

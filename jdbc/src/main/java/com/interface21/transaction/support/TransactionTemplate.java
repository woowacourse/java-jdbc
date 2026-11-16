package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Runs an action in a JDBC transaction whose connection is shared with JdbcTemplate through
 * TransactionSynchronizationManager. Business code only passes the action and never touches the connection.
 */
public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource must not be null");
    }

    public void executeWithoutResult(final Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    public <T> T execute(final Supplier<T> action) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            // Join the outer transaction; its owner decides commit, rollback and close.
            return action.get();
        }
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        Throwable failure = null;
        try {
            return beginAndRun(connection, action);
        } catch (SQLException e) {
            failure = e;
            throw new DataAccessException(e);
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource, failure);
        }
    }

    private <T> T beginAndRun(final Connection connection, final Supplier<T> action) throws SQLException {
        final boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            return runInTransaction(connection, action, autoCommit);
        } finally {
            // Unbind before release so that DataSourceUtils closes the connection instead of keeping it open.
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private <T> T runInTransaction(final Connection connection, final Supplier<T> action, final boolean autoCommit)
            throws SQLException {
        final T result;
        try {
            result = action.get();
            connection.commit();
        } catch (SQLException | RuntimeException | Error failure) {
            rollback(connection, autoCommit, failure);
            throw failure;
        }
        // Cleanup failures after a successful commit must not trigger another rollback.
        connection.setAutoCommit(autoCommit);
        return result;
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
            // Restoring autoCommit after a failed rollback could commit pending changes.
            return;
        }
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException restoreFailure) {
            failure.addSuppressed(restoreFailure);
        }
    }
}
package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionExecutor;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T runForObject(final TransactionExecutor<T> transactionExecutor) {
        return execute(transactionExecutor);
    }

    public void run(final Runnable runnable) {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T execute(final TransactionExecutor<T> transactionExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return runInConnection(transactionExecutor, connection);
        } catch (final SQLException e) {
            rollBackConnection(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private <T> T runInConnection(
            final TransactionExecutor<T> transactionExecutor,
            final Connection connection
    ) throws SQLException {
        connection.setAutoCommit(false);
        final T action = transactionExecutor.action();
        connection.commit();
        return action;
    }

    private void rollBackConnection(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

}

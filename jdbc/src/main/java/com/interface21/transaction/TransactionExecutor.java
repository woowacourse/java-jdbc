package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeVoid(final Runnable execution) {
        executeInSynchronizedTx(() -> {
            execution.run();
            return null; // void
        });
    }

    public <T> T execute(final Supplier<T> execution) {
        return executeInSynchronizedTx(execution);
    }

    private <T> T executeInSynchronizedTx(final Supplier<T> execution) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            return executeLogic(execution, connection);

        } finally {
            if (connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection);
            }
        }
    }

    private <T> T executeLogic(Supplier<T> execution, Connection connection) {
        try {
            connection.setAutoCommit(false);
            T result = execution.get();
            connection.commit();
            return result;

        } catch (final Exception e) {
            rollback(connection);
            throw new DataAccessException(e);

        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}

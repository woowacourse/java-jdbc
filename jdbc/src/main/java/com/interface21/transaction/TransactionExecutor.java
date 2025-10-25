package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeVoid(final Consumer<Connection> execution) {
        executeInSynchronizedTx(conn -> {
            execution.accept(conn);
            return null; // void
        });
    }

    public <T> T execute(final Function<Connection, T> execution) {
        return executeInSynchronizedTx(execution);
    }

    private <T> T executeInSynchronizedTx(final Function<Connection, T> execution) {
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

    private <T> T executeLogic(Function<Connection, T> execution, Connection connection) {
        try {
            connection.setAutoCommit(false);
            T result = execution.apply(connection);
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

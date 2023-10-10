package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable worker) {
        final Supplier<Void> workerToSupplier = () -> {
            worker.run();
            return null;
        };
        runWithTransaction(workerToSupplier);
    }

    private <T> T runWithTransaction(final Supplier<T> supplier) {
        final Connection connection = getTransactionalConnection();

        try {
            connection.setAutoCommit(false);
            final T result = supplier.get();
            connection.commit();

            return result;
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            setConnectionTransactionActive(false, connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private Connection getTransactionalConnection() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        setConnectionTransactionActive(true, connection);

        return connection;
    }

    private void setConnectionTransactionActive(final boolean isActive, final Connection connection) {
        final ConnectionHolder connectionHolder = new ConnectionHolder(connection);
        connectionHolder.setTransactionActive(isActive);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
    }

    private void rollback(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    public <T> T executeWithResult(final Supplier<T> worker) {
        return runWithTransaction(worker);
    }
}

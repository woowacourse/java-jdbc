package org.springframework.transaction;

import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.ConnectionManager;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transact(final Runnable runnable) {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T transact(final Supplier<T> supplier) {
        return execute(supplier);
    }

    private <R> R execute(final Supplier<R> transactionSupplier) {
        final ConnectionManager connectionManager = DataSourceUtils.getConnection(dataSource);
        try {
            connectionManager.activateTransaction();
            final R result = transactionSupplier.get();
            connectionManager.commit();
            return result;
        } catch (SQLException | DataAccessException exception) {
            rollback(connectionManager);
            throw new DataAccessException();
        } finally {
            connectionManager.inactivateTransaction();
            DataSourceUtils.releaseConnection(connectionManager, dataSource);
        }
    }

    private void rollback(final ConnectionManager connectionManager) {
        try {
            connectionManager.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException();
        }
    }
}

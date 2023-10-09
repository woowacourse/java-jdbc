package org.springframework.transaction;

import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.ConnectionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transact(final Runnable runnable) {
        final ConnectionManager connectionManager = DataSourceUtils.getConnection(dataSource);
        try {
            connectionManager.activeTransaction();
            runnable.run();
            connectionManager.commit();
        } catch (SQLException | DataAccessException exception) {
            rollback(connectionManager);
            throw new DataAccessException();
        } finally {
            connectionManager.inactiveTransaction();
            DataSourceUtils.releaseConnection(connectionManager, dataSource);
        }
    }

    public <T> T transact(final Supplier<T> supplier) {
        final ConnectionManager connectionManager = DataSourceUtils.getConnection(dataSource);
        try {
            connectionManager.activeTransaction();
            final T result = supplier.get();
            connectionManager.commit();
            return result;
        } catch (SQLException | DataAccessException exception) {
            rollback(connectionManager);
            throw new DataAccessException();
        } finally {
            connectionManager.inactiveTransaction();
            DataSourceUtils.releaseConnection(connectionManager, dataSource);
        }
    }

    private void close() {
        try {
            final ConnectionManager connectionManager = TransactionSynchronizationManager.unbindResource(dataSource);
            connectionManager.close();
        } catch (SQLException exception) {
            throw new DataAccessException();
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

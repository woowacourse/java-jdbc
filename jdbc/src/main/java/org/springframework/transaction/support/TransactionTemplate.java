package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionExecutor<T> executor) {
        final Connection connection = beginTransaction();
        try {
            final T result = executor.execute();
            commit(connection);
            return result;
        } catch (final DataAccessException e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private Connection beginTransaction() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
        return connection;
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}

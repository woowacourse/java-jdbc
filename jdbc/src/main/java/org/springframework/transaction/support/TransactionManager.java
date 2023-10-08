package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionExecutor<T> transactionExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final T result = transactionExecutor.execute();
            connection.commit();
            return result;
        } catch (final SQLException e) {
            rollback(connection);
        } finally {
            close(connection);
        }
        throw new DataAccessException();
    }

    public void executeNoReturn(final TransactionNoReturnExecutor transactionExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            transactionExecutor.execute();
            connection.commit();
        } catch (final SQLException e) {
            rollback(connection);
        } finally {
            close(connection);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    private void close(final Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}

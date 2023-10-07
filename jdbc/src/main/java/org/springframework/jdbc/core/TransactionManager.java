package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionExecutor<T> transactionExecutor) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            final T result = transactionExecutor.execute(connection);
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
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            transactionExecutor.execute(connection);
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
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
}

package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.exception.TransactionTemplateException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeQueryWithTransaction(final TransactionalCallbackWithReturnValue<T> callback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            setAutoCommit(connection, false);

            final T result = callback.execute();

            commit(connection);
            return result;
        } catch (final DataAccessException e) {
            rollback(connection);

            throw new TransactionTemplateException(e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void executeQueryWithTransaction(final TransactionalCallbackWithoutReturnValue callback) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            setAutoCommit(connection, false);
            callback.execute();
            commit(connection);
        } catch (final RuntimeException e) {
            rollback(connection);

            throw new TransactionTemplateException(e);
        } finally {
            releaseConnection(connection);
        }
    }

    private void setAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            throw new TransactionTemplateException(e);
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new TransactionTemplateException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new TransactionTemplateException(e);
        }
    }

    private void releaseConnection(final Connection connection) {
        setAutoCommit(connection, true);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }
}

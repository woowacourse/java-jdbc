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

    public <T> T execute(final TransactionServiceExecutor<T> tse) {
        try {
            start();
            final T result = tse.execute();
            commit();
            return result;
        } catch (final DataAccessException e) {
            rollback();
            throw e;
        }
    }

    private void start() {
        executeTransaction(connection -> connection.setAutoCommit(false));
    }

    private void commit() {
        executeTransaction(connection -> {
            connection.commit();
            close();
        });
    }

    private void rollback() {
        executeTransaction(connection -> {
            connection.rollback();
            close();
        });
    }

    private void close() {
        executeTransaction(connection -> DataSourceUtils.releaseConnection(connection, dataSource));
    }

    private void executeTransaction(final TransactionExecutor te) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            te.execute(connection);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

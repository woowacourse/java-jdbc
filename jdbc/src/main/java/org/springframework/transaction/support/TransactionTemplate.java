package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final Supplier<T> target) {
        if (TransactionSynchronizationManager.isTransactionActive(dataSource)) {
            return target.get();
        }
        final Connection connection = beginTransaction(dataSource);
        try {
            final T result = target.get();

            commit(connection);
            return result;
        } catch (final Exception e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection beginTransaction(final DataSource dataSource) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            return connection;
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T executeWithResult(final Supplier<T> target) {
        return execute(target);
    }

    public void execute(final Runnable target) {
        execute(() -> {
            target.run();
            return null;
        });
    }
}

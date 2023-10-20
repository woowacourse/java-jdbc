package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public <T> T execute(final Supplier<T> transactionalFunction) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            final T value = transactionalFunction.get();

            conn.commit();
            return value;
        } catch (final SQLException e) {
            rollback(conn);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        return null;
    }

    public void execute(final Runnable transactionalFunction) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            transactionalFunction.run();

            conn.commit();
        } catch (final SQLException e) {
            rollback(conn);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

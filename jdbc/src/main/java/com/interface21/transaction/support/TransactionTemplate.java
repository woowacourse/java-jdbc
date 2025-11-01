package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> action) {
        final Connection existingConn = TransactionSynchronizationManager.getResource(dataSource);

        if (existingConn != null) {
            return action.doInTransaction();
        }

        return executeWithNewTransaction(action);
    }

    public void execute(final Runnable action) {
        final Connection existingConn = TransactionSynchronizationManager.getResource(dataSource);

        if (existingConn != null) {
            action.run();
            return;
        }

        executeWithNewTransaction(action);
    }

    private <T> T executeWithNewTransaction(final TransactionCallback<T> action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);

        try {
            conn.setAutoCommit(false);
            final T result = action.doInTransaction();
            conn.commit();
            return result;
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw new DataAccessException("Transaction failed", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void executeWithNewTransaction(final Runnable action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, conn);

        try {
            conn.setAutoCommit(false);
            action.run();
            conn.commit();
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw new DataAccessException("Transaction failed", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollbackQuietly(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("Failed to rollback transaction", e);
        }
    }
}

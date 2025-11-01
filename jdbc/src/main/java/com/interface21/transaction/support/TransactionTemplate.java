package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            final T result = action.doInTransaction();

            conn.commit();
            return result;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Rollback failed", rollbackEx);
            }
            throw new DataAccessException("Transaction failed", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void execute(final Runnable action) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            action.run();
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Rollback failed", rollbackEx);
            }
            throw new DataAccessException("Transaction failed", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

}

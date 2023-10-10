package org.springframework.jdbc.core.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxExecutor {

    public static void executeWithoutReturnValue(
            final DataSource dataSource,
            final ExecutorWithoutReturnValue executor
    ) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            executor.execute();

            commit(conn);
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public static Object execute(
            final DataSource dataSource,
            final Executor executor
    ) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            final Object result = executor.execute();

            conn.commit();
            return result;
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void commit(final Connection conn) {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

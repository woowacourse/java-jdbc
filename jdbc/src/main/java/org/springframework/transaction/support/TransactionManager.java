package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable logicExecutor) {
        executeWithResult(() -> {
            logicExecutor.run();
            return null;
        });
    }

    public <T> T executeWithResult(final Supplier<T> logicExecutor) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        setTransactionActive(conn);
        try {
            conn.setAutoCommit(false);
            T result = logicExecutor.get();
            conn.commit();
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            setTransactionInactive(conn);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void setTransactionActive(final Connection conn) {
        ConnectionHolder connectionHolder = new ConnectionHolder(conn);
        connectionHolder.setConnectionTransactionActive(true);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
    }

    private void setTransactionInactive(final Connection conn) {
        ConnectionHolder connectionHolder = new ConnectionHolder(conn);
        connectionHolder.setConnectionTransactionActive(false);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

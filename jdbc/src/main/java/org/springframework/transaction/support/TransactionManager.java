package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.exception.TransactionException;

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
        setTransactionActiveStatus(conn, true);
        try {
            conn.setAutoCommit(false);
            T result = logicExecutor.get();
            conn.commit();
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new TransactionException(e);
        } finally {
            setTransactionActiveStatus(conn, false);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void setTransactionActiveStatus(final Connection conn, final boolean isTransactionActive) {
        ConnectionHolder connectionHolder = new ConnectionHolder(conn);
        connectionHolder.setConnectionTransactionActive(isTransactionActive);
        TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }
}

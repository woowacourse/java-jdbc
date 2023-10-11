package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T doTransaction(Supplier<T> transactionExecutor) {
        return executeTransaction(transactionExecutor);
    }

    public void doTransaction(Runnable transactionExecutor) {
        executeTransaction(() -> {
            transactionExecutor.run();
            return null;
        });
    }

    private <T> T executeTransaction(Supplier<T> transactionExecutor) {
        T result = null;
        try (final var conn = DataSourceUtils.getConnection(dataSource)) {
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            conn.setAutoCommit(false);
            result = transactionExecutor.get();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (final SQLException e) {
            rollBack(e, TransactionSynchronizationManager.getResource(dataSource));
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        return result;
    }

    private void rollBack(final SQLException e, final Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
            throw new DataAccessException(e);
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}

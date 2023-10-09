package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void service(TransactionExecutor transactionExecutor) {
        try (final var conn = DataSourceUtils.getConnection(dataSource)) {
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            conn.setAutoCommit(false);
            transactionExecutor.execute();
            conn.commit();
        } catch (final SQLException e) {
            rollBack(e, TransactionSynchronizationManager.getResource(dataSource));
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollBack(final SQLException e, final Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException(e);
        } catch (final SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}

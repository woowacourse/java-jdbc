package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTransactionTemplate.class);

    private final DataSource dataSource;

    public JdbcTransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable action) {
        final var existing = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (existing != null) {
            try {
                action.run();
            } catch (RuntimeException | Error failure) {
                existing.rollbackOnly = true;
                throw failure;
            }
            return;
        }

        boolean committed = false;
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            final var transaction = TransactionSynchronizationManager.getConnectionHolder(dataSource);
            try {
                action.run();
                if (transaction.rollbackOnly) {
                    throw new DataAccessException("Transaction was marked rollback-only");
                }
                connection.commit();
                committed = true;
            } catch (SQLException | RuntimeException | Error failure) {
                rollback(connection, autoCommit, failure);
                throw failure;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (committed) {
                log.warn("Failed to clean up JDBC connection after committing transaction", e);
                return;
            }
            throw new DataAccessException("Failed to execute JDBC transaction", e);
        }
    }

    private void rollback(final Connection connection, final boolean autoCommit, final Throwable failure) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure.addSuppressed(e);
        }
    }
}

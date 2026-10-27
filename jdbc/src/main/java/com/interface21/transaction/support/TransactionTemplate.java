package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable action) {
        Objects.requireNonNull(action);
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("중첩 트랜잭션은 지원하지 않습니다.");
        }

        final var connection = DataSourceUtils.getConnection(dataSource);
        Throwable originalException = null;
        boolean bound = false;
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            bound = true;
            execute(connection, action);
        } catch (SQLException e) {
            final var exception = new DataAccessException(e);
            originalException = exception;
            throw exception;
        } catch (RuntimeException | Error e) {
            originalException = e;
            throw e;
        } finally {
            if (bound) {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
            DataSourceUtils.releaseConnection(connection, dataSource, originalException);
        }
    }

    private void execute(final Connection connection, final Runnable action) throws SQLException {
        final var autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            action.run();
            connection.commit();
        } catch (SQLException | RuntimeException | Error e) {
            rollback(connection, autoCommit, e);
            throw e;
        }
        connection.setAutoCommit(autoCommit);
    }

    private void rollback(final Connection connection, final boolean autoCommit,
                          final Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException | RuntimeException | Error rollbackException) {
            if (originalException != rollbackException) {
                originalException.addSuppressed(rollbackException);
            }
            return;
        }
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException | RuntimeException | Error restoreException) {
            if (originalException != restoreException) {
                originalException.addSuppressed(restoreException);
            }
        }
    }
}

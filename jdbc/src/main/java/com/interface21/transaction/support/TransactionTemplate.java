package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable action) {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("A transaction is already active for this DataSource");
        }

        Connection connection = null;
        Throwable failure = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                action.run();
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollback(connection, e);
                throw e;
            }
        } catch (SQLException | CannotGetJdbcConnectionException e) {
            final var dataAccessException = new DataAccessException(e);
            failure = dataAccessException;
            throw dataAccessException;
        } catch (RuntimeException | Error e) {
            failure = e;
            throw e;
        } finally {
            if (connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                releaseConnection(connection, failure);
            }
        }
    }

    private void rollback(Connection connection, Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    private void releaseConnection(Connection connection, Throwable originalException) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (CannotGetJdbcConnectionException e) {
            if (originalException != null) {
                originalException.addSuppressed(e);
                return;
            }
            throw new DataAccessException(e);
        }
    }
}

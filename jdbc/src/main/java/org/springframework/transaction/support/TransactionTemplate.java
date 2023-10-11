package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nullable
    public <T> T execute(final TransactionCallback<T> action) {
        final T result;
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            result = action.doInTransaction();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException("Failed to change password.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        return result;
    }

    public void executeWithoutResult(final Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            throw new DataAccessException("Failed to rollback transaction.", rollbackException);
        }
    }
}

package com.techcourse.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithoutResult(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    public <T> T execute(Supplier<T> action) {
        final boolean isExistingTransaction = TransactionSynchronizationManager.hasResource(dataSource);

        if (isExistingTransaction) {
            return action.get();
        }

        final Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originalAutoCommit = true;
        boolean autoCommitChanged = false;

        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            originalAutoCommit = connection.getAutoCommit();
            if (originalAutoCommit) {
                connection.setAutoCommit(false);
                autoCommitChanged = true;
            }

            try {
                T result = action.get();
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            if (autoCommitChanged) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                }
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}

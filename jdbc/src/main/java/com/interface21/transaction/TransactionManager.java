package com.interface21.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

public class TransactionManager {

    private TransactionManager() {
    }

    public static void executeInTransaction(final DataSource dataSource, final Runnable action) {
        try {
            executeInTransaction(dataSource, () -> {
                action.run();
                return null;
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T executeInTransaction(final DataSource dataSource, final Supplier<T> action)
        throws SQLException {

        boolean isNewTransaction = isNewTransaction(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);

        if (isNewTransaction) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        }

        try {
            boolean originAutoCommit = connection.getAutoCommit();

            if (isNewTransaction) {
                connection.setAutoCommit(false);
            }
            return getResult(action, isNewTransaction, connection, originAutoCommit);
        } catch (SQLException e) {
            if (isNewTransaction) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (isNewTransaction) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private static <T> T getResult(
        final Supplier<T> action,
        final boolean isNewTransaction,
        final Connection connection,
        final boolean originAutoCommit
    ) throws SQLException {
        try {
            T result = action.get();
            if (isNewTransaction) {
                connection.commit();
            }
            return result;
        } catch (RuntimeException e) {
            if (isNewTransaction) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (isNewTransaction) {
                connection.setAutoCommit(originAutoCommit);
            }
        }
    }

    private static boolean isNewTransaction(final DataSource dataSource) {
        Connection existingConnection = TransactionSynchronizationManager.getResource(dataSource);
        return existingConnection == null;
    }
}

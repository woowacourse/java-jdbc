package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(Runnable runnable) {
        executeInTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeInTransaction(Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();

            return result;
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Rollback failed: " + e.getMessage(), e);
        }
    }
}

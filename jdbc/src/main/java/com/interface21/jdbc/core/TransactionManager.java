package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import javax.sql.DataSource;

public class TransactionManager {

    public static void doInTransaction(DataSource dataSource, Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionManager.startTransaction(connection);
            runnable.run();
            TransactionManager.commitTransaction(connection);
        } catch (Exception e) {
            TransactionManager.rollbackTransaction(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public static <T> T doInTransaction(DataSource dataSource, Callable<T> callable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionManager.startTransaction(connection);
            T result = callable.call();
            TransactionManager.commitTransaction(connection);
            return result;
        } catch (Exception e) {
            TransactionManager.rollbackTransaction(connection);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void startTransaction(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static void commitTransaction(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}

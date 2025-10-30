package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private TransactionManager() {
    }

    public static <T> T doInReadOnlyTransaction(DataSource dataSource, Callable<T> callable) {
        boolean isTransactionActive = TransactionSynchronizationManager.isTransactionActive(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (!isTransactionActive) {
                TransactionSynchronizationManager.setTransactionActive(dataSource, true);
                connection.setReadOnly(true);
            }
            return callable.call();
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            if (!isTransactionActive) {
                TransactionSynchronizationManager.setTransactionActive(dataSource, false);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    public static void doInTransaction(DataSource dataSource, TransactionCallback callback) {
        doInTransaction(dataSource, () -> {
            callback.execute();
            return null;
        });
    }

    public static <T> T doInTransaction(DataSource dataSource, Callable<T> callable) {
        boolean isTransactionActive = TransactionSynchronizationManager.isTransactionActive(dataSource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            if (!isTransactionActive) {
                TransactionSynchronizationManager.setTransactionActive(dataSource, true);
                connection.setAutoCommit(false);
            }
            T result = callable.call();
            if (!isTransactionActive) {
                connection.commit();
            }
            return result;
        } catch (Exception e) {
            rollbackIfNewTransaction(isTransactionActive, connection);
            throw new DataAccessException(e);
        } finally {
            if (!isTransactionActive) {
                TransactionSynchronizationManager.setTransactionActive(dataSource, false);
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

    private static void rollbackIfNewTransaction(boolean isTransactionActive, Connection connection) {
        if (isTransactionActive) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException sqlException) {
            logger.error("롤백 실패", sqlException);
        }
    }
}

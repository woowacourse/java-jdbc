package com.interface21.jdbc.manager;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionManager {

    private static final String TRANSACTION_FAIL_EXCEPTION = "Transaction을 실행하던 도중 실패했습니다.";

    private TransactionManager() {
    }

    public static <T> T start(Connection connection, Supplier<T> supplier, DataSource dataSource) {
        try {
            connection.setAutoCommit(false);
            T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public static void start(Connection connection, Runnable runnable, DataSource dataSource) {
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        }
    }
}

package com.techcourse.support.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}

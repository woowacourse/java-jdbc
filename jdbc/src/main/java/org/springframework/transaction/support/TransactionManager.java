package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final ServiceCallback executor) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            executor.doGetTransaction();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public <T> T execute(final ServiceExecutor<T> executor) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final T result = executor.doGetTransaction();
            connection.commit();
            return result;
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}

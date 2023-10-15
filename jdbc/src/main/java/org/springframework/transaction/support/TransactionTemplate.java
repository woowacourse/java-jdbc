package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionExecutor<T> executor) {
        Connection connection = startTransaction();
        try {
            T result = executor.execute();
            commit(connection);
            return result;
        } catch (DataAccessException e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private Connection startTransaction() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
        return connection;
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}

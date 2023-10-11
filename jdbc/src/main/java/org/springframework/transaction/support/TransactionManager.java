package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        final Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            releaseConnection(connection);
        }
    }

    private Connection getConnection() {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.setActiveTransaction(connection, true);
        return connection;
    }

    private void releaseConnection(final Connection connection) {
        TransactionSynchronizationManager.setActiveTransaction(connection, false);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public <T> T executeWithResult(final Supplier<T> supplier) {
        final Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            final T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataSourceTransactionManager implements TransactionManager {

    private final DataSource dataSource;

    private DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static DataSourceTransactionManager init(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Override
    public void begin() {
        final Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            releaseConnection();
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    @Override
    public void commit() {
        final Connection connection = getConnection();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection();
        }
    }

    @Override
    public void rollback() {
        final Connection connection = getConnection();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            releaseConnection();
        }
    }

    private Connection getConnection() {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == null) {
            connection = DataSourceUtils.getNewConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        }
        return connection;
    }

    private void releaseConnection() {
        final Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
        if (TransactionSynchronizationManager.hasNotResource(dataSource)) {
            DataSourceUtils.releaseConnection(connection);
        }
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void commit() {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        try {
            connection.commit();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void rollback() {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        try {
            connection.rollback();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void clear() {
        try {
            final Connection connection = TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}

package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void startTransaction() {
        try {
            final Connection connection = getConnection();
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            final Connection connection = getConnection();
            connection.commit();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            final Connection connection = getConnection();
            connection.rollback();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanUp() {
        final Connection connection = getConnection();
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}

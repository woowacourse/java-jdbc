package com.interface21.transaction;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class SimpleTransactionManager implements TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(SimpleTransactionManager.class);

    private final DataSource dataSource;
    private Connection connection;

    public SimpleTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void begin() {
        this.connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionException("Failed to disable auto-commit", e);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new TransactionException("Failed to commit transaction", e);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new TransactionException("Failed to rollback transaction", e);
        }
    }

    @Override
    public void cleanup() {
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (final Exception e) {
            log.warn("Failed to unbind resource", e);
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (final Exception e) {
                log.warn("Failed to release connection", e);
            }
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
}

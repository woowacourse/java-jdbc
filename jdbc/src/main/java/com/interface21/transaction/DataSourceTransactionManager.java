package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceTransactionManager implements PlatformTransactionManager {

    private static final Logger log = LoggerFactory.getLogger(DataSourceTransactionManager.class);

    private final DataSource dataSource;

    public DataSourceTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        return TransactionSynchronizationManager.getResource(dataSource);
    }

    public void bindConnection() {
        try {
            final var connection = dataSource.getConnection();

            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            log.debug("Bound connection : [{}] ", connection);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void commit() {
        final var connection = getConnection();
        try {
            log.debug("Committing JDBC transaction on connection [{}]", connection);
            connection.commit();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        } finally {
            release();
        }
    }

    @Override
    public void rollback() {
        final var connection = getConnection();
        try {
            log.debug("Rolling back JDBC transaction on connection [{}]", connection);
            connection.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        } finally {
            release();
        }
    }

    private void release() {
        final var connection = TransactionSynchronizationManager.unbindResource(dataSource);
        try {
            connection.close();
        } catch (final SQLException e) {
            log.debug("Could not close JDBC Connection", e);
        }
    }
}

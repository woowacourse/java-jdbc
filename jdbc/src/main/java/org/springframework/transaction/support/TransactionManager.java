package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() {
        try {
            final Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            TransactionSynchronizationManager.bindResource(dataSource, conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        final Connection conn = getThreadLocalConnection();

        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private Connection getThreadLocalConnection() {
        final Connection conn = TransactionSynchronizationManager.getResource(dataSource);
        if (conn == null) {
            throw new DataAccessException("ThreadLocal Connection Is Null");
        }

        return conn;
    }

    public void rollback() {
        final Connection conn = getThreadLocalConnection();

        try {
            conn.rollback();
        } catch (SQLException e) {
            log.warn("Transaction Error, Rollback", e);
            throw new DataAccessException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

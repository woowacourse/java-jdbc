package com.interface21.transaction.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class AbstractTransactionManager implements PlatformTransactionManager {

    private final DataSource dataSource;

    public AbstractTransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            cleanup(conn);
            throw new DataAccessException("Transaction init error", e);
        }
    }

    @Override
    public void commit() {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Transaction commit error", e);
        } finally {
            cleanup(conn);
        }
    }

    @Override
    public void rollback() {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Transaction rollback error", e);
        } finally {
            cleanup(conn);
        }
    }

    private void cleanup(final Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("Could not reset auto-commit after transaction", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

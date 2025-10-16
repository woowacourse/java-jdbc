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
        try {
            final Connection conn = DataSourceUtils.getConnection(dataSource);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Transaction init error", e);
        }
    }

    @Override
    public void commit() {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("Transaction commit error", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void rollback() {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException("Transaction rollback error", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

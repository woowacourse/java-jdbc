package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            begin(conn);
            final T result = callback.doInTransaction(conn);
            commit(conn);
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Transaction failed and rolled back", e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void begin(final Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (Exception e) {
            throw new DataAccessException("Transaction begin error", e);
        }
    }

    private void commit(final Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            throw new DataAccessException("Transaction commit error", e);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            throw new DataAccessException("Transaction rollback error", e);
        }
    }
}

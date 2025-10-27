package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> callback) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            T result = callback.doInTransaction();

            conn.commit();
            return result;

        } catch (Exception e) {
            log.atError().log("Transaction is being rolled back", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DataAccessException("Rollback failed: " + ex.getMessage(), ex);
                }
            }
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    log.atError().log("Error setting autoCommit", e);
                }
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }

    public <T> T executeReadOnly(TransactionCallback<T> callback) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return callback.doInTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Read-only operation failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
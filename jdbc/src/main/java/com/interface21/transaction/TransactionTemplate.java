package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/transaction/support/TransactionTemplate.java
public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionCallback<T> callback) {
        final var conn = DataSourceUtils.getConnection(dataSource);
        try {
            return commit(callback, conn);
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            close();
        }
    }

    private <T> T commit(final TransactionCallback<T> callback, final Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        T result = callback.doInTransaction();
        conn.commit();
        return result;
    }

    private void rollback(final Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                log.error("Rollback failed: {}", ex.getMessage(), ex);
            }
        }
    }

    private void close() {
        try {
            final var connection = TransactionSynchronizationManager.unbindResource(dataSource);
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Connect Close failed: " + e.getMessage(), e);
        }
    }
}

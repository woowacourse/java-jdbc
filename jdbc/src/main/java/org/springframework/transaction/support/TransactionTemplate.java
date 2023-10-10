package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        if (TransactionSynchronizationManager.isTransactionEnable()) {
            runnable.run();
            return;
        }
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        begin(conn);
        try {
            runnable.run();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void begin(final Connection connection) {
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.begin();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

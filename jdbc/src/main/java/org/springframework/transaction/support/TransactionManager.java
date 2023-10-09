package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(Runnable runnable) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            runnable.run();
            conn.commit();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

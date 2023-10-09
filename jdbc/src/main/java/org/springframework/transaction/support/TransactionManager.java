package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(final Runnable runnable) {
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

    public <T> T executeInTransaction(final Supplier<T> supplier) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            final T result = supplier.get();
            conn.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}

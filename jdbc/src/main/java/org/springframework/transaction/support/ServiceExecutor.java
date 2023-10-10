package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class ServiceExecutor {

    private final DataSource dataSource;

    public ServiceExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionTemplate<T> executor) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            final T result = executor.execute();
            conn.commit();
            return result;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}

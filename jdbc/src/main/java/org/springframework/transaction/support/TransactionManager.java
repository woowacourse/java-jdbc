package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doInTransaction(final Runnable runnable) throws DataAccessException {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (final SQLException e) {
            doRollback(e, connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void doRollback(final SQLException e, final Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException(e);
        } catch (final SQLException ex) {
            throw new DataAccessException("rollback failed by : " + e.getMessage(), ex);
        }
    }
}

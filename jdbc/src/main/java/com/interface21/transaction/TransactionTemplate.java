package com.interface21.transaction;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> action) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            T result = action.doInTransaction();
            connection.commit();
            return result;

        } catch (SQLException e) {
            rollback(connection);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new RuntimeException("Rollback failed", ex);
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction();
    }
}

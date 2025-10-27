package com.interface21.transaction;

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
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, connection);

            T result = action.doInTransaction();

            connection.commit();
            return result;

        } catch (RuntimeException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Rollback failed", ex);
                }
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction();
    }
}

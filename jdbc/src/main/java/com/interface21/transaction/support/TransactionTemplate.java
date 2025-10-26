package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> callback) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(dataSource, connection);

            T result = callback.doInTransaction();

            connection.commit();
            return result;
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                }
            }
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                try {
                    connection.close();
                } catch (SQLException ex) {
                }
            }
        }
    }
}

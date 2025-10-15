package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);
    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public T execute(ServiceCallback<T> serviceCallback) {
        boolean isExistingTransaction = TransactionSynchronizationManager.getResource(dataSource) != null;

        if (isExistingTransaction) {
            return serviceCallback.execute();
        }

        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            T result = serviceCallback.execute();

            connection.commit();
            return result;
        } catch (Exception e) {
            rollback(e, connection);
        } finally {
            closeConnection(connection);
        }
        return null;
    }

    private void rollback(Exception e, Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException x) {
                log.error("Failed to rollback transaction", x);
            }
        }
        throw new DataAccessException(e);
    }

    private void closeConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);

        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close JDBC Connection", e);
            }
        }                                   
    }
}

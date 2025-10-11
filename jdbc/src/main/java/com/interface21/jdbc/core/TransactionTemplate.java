package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setConnection(connection);

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

    private static void rollback(Exception e, Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException x) {
                log.error("Failed to rollback transaction", x);
            }
        }
        throw new DataAccessException(e);
    }

    private static void closeConnection(Connection connection) {
        TransactionSynchronizationManager.closeConnection();
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}

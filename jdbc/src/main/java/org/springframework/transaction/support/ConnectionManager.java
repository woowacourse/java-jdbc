package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectionManager {

    private ConnectionManager() {
    }

    public static void close(DataSource dataSource, Connection connection) {
        try {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (Exception closeException) {
            throw new DataAccessException("failed to close connection", closeException);
        }
    }

    public static void rollback(Exception e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            throw new DataAccessException("failed to rollback transaction", rollbackException);
        }
        throw new DataAccessException(e);
    }
}

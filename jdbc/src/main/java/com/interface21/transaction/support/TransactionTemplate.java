package com.interface21.transaction.support;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.TransactionException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> action) {
        Connection connection = getConnection();
        try {
            T result = action.doInTransaction(connection);
            commit(connection);
            return result;
        } catch (RuntimeException e) {
            rollback(connection);
            throw e;
        } finally {
            releaseConnection(connection);
        }
    }

    private Connection getConnection() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new TransactionException("JDBC commit failed", e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException("JDBC rollback failed", e);
        }
    }

    private void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", e);
        }
    }
}

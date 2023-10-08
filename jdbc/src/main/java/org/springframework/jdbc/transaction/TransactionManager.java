package org.springframework.jdbc.transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.jdbc.datasource.DataSourceUtils.*;
import static org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection;
import static org.springframework.transaction.support.TransactionSynchronizationManager.*;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionExecutor<T> transactionExecutor) {
        try {
            Connection connection = begin();
            T response = transactionExecutor.execute(connection);
            commit();
            return response;
        } catch (RuntimeException e) {
            rollback();
            throw e;
        }
    }

    public Connection begin() {
        Connection connection = getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void commit() {
        Connection connection = getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        releaseConnection(connection, dataSource);
    }

    public void rollback() {
        Connection connection = getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        releaseConnection(connection, dataSource);
    }

}

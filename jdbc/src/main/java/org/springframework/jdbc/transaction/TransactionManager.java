package org.springframework.jdbc.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private static final ThreadLocal<Connection> connectionInThread = new ThreadLocal<>();

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
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        connectionInThread.set(connection);
        return connection;
    }

    public void commit() {
        Connection connection = getConnection();
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        init(connection);
    }

    public void rollback() {
        Connection connection = getConnection();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        init(connection);
    }

    private void init(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        connectionInThread.remove();
    }

    public Connection getConnection() {
        if (connectionInThread.get() == null) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return connectionInThread.get();
    }

}

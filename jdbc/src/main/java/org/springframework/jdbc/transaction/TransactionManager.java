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

    public void begin(final Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        connectionInThread.set(connection);
        getConnection();
    }

    public void commit() {
        Connection connection = getConnection();
        try {
            connection.commit();
            connection.close();
            connectionInThread.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            connection.close();
            connectionInThread.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

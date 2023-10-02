package org.springframework.transaction.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        if (connectionThreadLocal.get() == null) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return connectionThreadLocal.get();
    }

    public Connection start() {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            connectionThreadLocal.set(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            Connection connection = getConnection();
            connection.commit();
            connection.close();
            connectionThreadLocal.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            connection.close();
            connectionThreadLocal.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

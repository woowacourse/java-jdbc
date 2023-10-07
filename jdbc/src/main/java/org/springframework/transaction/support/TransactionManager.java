package org.springframework.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final ConnectionHolder connectionHolder;
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource, ConnectionHolder connectionHolder) {
        this.dataSource = dataSource;
        this.connectionHolder = connectionHolder;
    }

    public Connection getConnection() {
        if (connectionHolder.isEmpty()) {
            return newConnection();
        }
        return connectionHolder.getConnection();
    }

    private Connection newConnection() {
        try {
            Connection connection = dataSource.getConnection();
            connectionHolder.setConnection(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection start() {
        try {
            Connection connection = getConnection();
            connection.setAutoCommit(false);
            connectionHolder.setConnection(connection);
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
            connectionHolder.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection connection = getConnection();
            connection.rollback();
            connection.close();
            connectionHolder.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.springframework.transaction.support;

import org.springframework.jdbc.exception.DatabaseResourceException;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private final Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void start() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseResourceException("setAutoCommit fails", e);
        }
    }

    public void commit() {
        try {
            connection.commit();
            close(connection);
        } catch (SQLException e) {
            throw new DatabaseResourceException("Commit fails.", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
            close(connection);
        } catch (SQLException e) {
            throw new DatabaseResourceException("Rollback fails.", e);
        }
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseResourceException("Connection close fails.", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}

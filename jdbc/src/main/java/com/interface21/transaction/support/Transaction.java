package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {
    private final Connection connection;

    public Transaction(final Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void begin() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public void close() throws SQLException {
        connection.close();
    }
}

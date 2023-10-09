package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private final Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
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
}

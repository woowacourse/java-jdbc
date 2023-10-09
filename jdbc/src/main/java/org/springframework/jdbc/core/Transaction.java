package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private final Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

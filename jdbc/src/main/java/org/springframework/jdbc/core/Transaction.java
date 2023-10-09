package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transaction {

    private final Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public <V> V execute(String sql, PreparedStatementExecutor<V> preparedStatementExecutor) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
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

package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransaction {

    private final Connection connection;

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void release() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

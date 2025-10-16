package com.techcourse.service;

import com.interface21.jdbc.CustomizedDataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionService {

    private final DataSource dataSource;

    public TransactionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Transaction begin() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return new Transaction(connection);
        } catch (SQLException e) {
            throw new CustomizedDataAccessException("connection failed", e);
        }
    }

    public static class Transaction implements AutoCloseable {

        private final Connection connection;
        private boolean isComplete = false;

        public Transaction(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }

        public void commit() {
            try {
                connection.commit();
                isComplete = true;
            } catch (SQLException e) {
                throw new CustomizedDataAccessException("commit failed", e);
            }
        }

        public void rollback() {
            try {
                connection.rollback();
                isComplete = true;
            } catch (SQLException e) {
                throw new CustomizedDataAccessException("rollback failed", e);
            }
        }

        @Override
        public void close() {
            try {
                if (!isComplete) {
                    rollback();
                }
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException("close connection fail", e);
                }
            }
        }
    }
}

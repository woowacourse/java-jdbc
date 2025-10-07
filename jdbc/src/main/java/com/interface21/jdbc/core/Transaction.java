package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {

    private static final Logger log = LoggerFactory.getLogger(Transaction.class);

    private Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void start() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            // TODO : 예외처리 강화 필요
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            // TODO : 예외처리 강화 필요
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
            connection.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            // TODO : 예외처리 강화 필요
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}

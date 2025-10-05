package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {

    private static final Logger log = LoggerFactory.getLogger(Transaction.class);

    private final DataSource dataSource;
    private Connection connection;
    private boolean isStarted = false;

    public Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            this.connection = connection;
            this.isStarted = true;
            TransactionHolder.setTransaction(this);
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
            this.connection = null;
            this.isStarted = false;
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
            this.connection = null;
            this.isStarted = false;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            // TODO : 예외처리 강화 필요
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isStarted() {
        return isStarted;
    }
}

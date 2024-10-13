package com.interface21.transaction;

import com.interface21.jdbc.exception.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    public TransactionManager() {
    }

    public void transaction(Connection connection, Runnable runnable) {
        try {
            startTransaction(connection, runnable);
        } catch (CannotGetJdbcConnectionException e) {
            log.error(e.getMessage());
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }

    private void startTransaction(Connection connection, Runnable runnable) {
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (Exception e) {
            System.out.println("rollback 시작...");
            log.error(e.getMessage());
            rollback(connection);
            throw new CannotGetJdbcConnectionException("Rollback : " + e.getMessage());
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new CannotGetJdbcConnectionException(e.getMessage());
        }
    }
}

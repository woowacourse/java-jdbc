package com.interface21.jdbc.manager;

import com.interface21.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private static final String TRANSACTION_FAIL_EXCEPTION = "Transaction을 실행하던 도중 실패했습니다.";

    private TransactionManager() {
    }

    public static void start(Connection connection, Runnable runnable) {
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        }
    }
}

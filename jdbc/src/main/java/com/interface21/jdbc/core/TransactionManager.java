package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.jdbc.exception.ConnectionCloseException;
import com.interface21.jdbc.exception.SqlExecutionException;
import com.interface21.jdbc.exception.TransactionExecutionException;
import com.interface21.jdbc.exception.TransactionRollbackException;

public class TransactionManager {

    private TransactionManager() {
    }

    public static void execute(final Connection connection, final Runnable runnable) {
        try {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (final SQLException | SqlExecutionException e) {
            rollback(connection);
            throw new TransactionExecutionException("트랜잭션 실행에 실패하였습니다.", e);
        } finally {
            closeConnection(connection);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new TransactionRollbackException("롤백에 실패하였습니다.", e);
        }
    }

    private static void closeConnection(final Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (final SQLException e) {
            throw new ConnectionCloseException("커넥션이 닫혀있습니다.", e);
        }
    }
}

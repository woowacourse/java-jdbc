package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;

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
        }
        //TODO 커넥션 닫기 어케하지
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new TransactionRollbackException("롤백에 실패하였습니다.");
        }
    }
}

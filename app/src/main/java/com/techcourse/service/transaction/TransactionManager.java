package com.techcourse.service.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.jdbc.core.exception.JdbcSQLException;
import com.techcourse.config.DataSourceConfig;

public class TransactionManager {

    private TransactionManager() {}

    public static void runTransaction(TransactionExecutor executor) {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {

            executeWithinConnectionTransaction(connection, executor);
        } catch (SQLException e) {
            throw new JdbcSQLException(e);
        }
    }

    private static void executeWithinConnectionTransaction(Connection connection, TransactionExecutor executor)
            throws SQLException {
        try {
            connection.setAutoCommit(false);
            executor.execute(connection);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }
}

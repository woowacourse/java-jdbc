package com.interface21.transaction.manager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.jdbc.core.exception.JdbcSQLException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private TransactionManager() {}

    public static void runTransaction(TransactionExecutor executor, DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            executeWithinConnectionTransaction(connection, executor);
        } catch (SQLException e) {
            throw new JdbcSQLException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void executeWithinConnectionTransaction(Connection connection, TransactionExecutor executor)
            throws SQLException {
        try {
            connection.setAutoCommit(false);
            executor.execute();
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        }
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }
        // 트랜잭션 경계에 포함된 커넥션인 경우 - 트랜잭션 경계에서 직접 해제
        final var transactionBound = TransactionSynchronizationManager.getResource(dataSource);
        if (transactionBound != null && transactionBound == connection) {
            return;
        }
        // 비트랜잭션 경계의 커넥션인 경우 | 트랜잭션이 종료된 경우 - 바로 해제
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

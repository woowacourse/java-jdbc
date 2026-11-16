package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (isTransactional(connection, dataSource)) {
            return; // 트랜잭션 중이면 트랜잭션을 시작한 쪽이 정리한다
        }

        // 닫기에 실패해도 보관함에 남지 않도록 먼저 뺀다
        if (TransactionSynchronizationManager.getResource(dataSource) == connection) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }

    // 보관함에 묶여 있고 autoCommit이 꺼져 있으면 트랜잭션 중인 커넥션이다
    private static boolean isTransactional(Connection connection, DataSource dataSource) {
        if (TransactionSynchronizationManager.getResource(dataSource) != connection) {
            return false;
        }
        try {
            return !connection.getAutoCommit();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to check JDBC Connection auto-commit", ex);
        }
    }
}

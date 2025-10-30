package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (final SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (connection == null) {
            return;
        }

        // 트랜잭션 내부에서 관리되는 Connection인지 확인
        final Connection transactionConnection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == transactionConnection) {
            // 트랜잭션 내부의 Connection은 JdbcTemplate에서 닫지 않음
            // TxUserService에서 트랜잭션 종료 시 닫힘
            return;
        }

        // 트랜잭션 외부의 Connection은 바로 닫음
        try {
            connection.close();
        } catch (final SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

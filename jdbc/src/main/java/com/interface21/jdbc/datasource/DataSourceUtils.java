package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * 트랜잭션 동기화를 지원하여, 트랜잭션이 활성화된 경우 동일한 Connection을 재사용한다.
 */
public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        // 1. 트랜잭션 동기화 매니저에서 이미 바인딩된 Connection이 있는지 확인
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            // 트랜잭션이 활성화되어 있으면 동일한 Connection 반환
            return connection;
        }

        // 2. 새로운 Connection을 생성하고 트랜잭션 동기화 매니저에 바인딩
        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        // 트랜잭션 동기화된 Connection인지 확인
        Connection boundConnection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == boundConnection) {
            // 트랜잭션 동기화된 Connection이면 닫지 않음
            return;
        }

        // 일반적인 경우에는 Connection을 닫음
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

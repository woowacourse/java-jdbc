package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    // 트랜잭션이 진행 중이면 바인딩된 Connection을, 아니면 새 Connection을 반환한다.
    // 바인딩은 트랜잭션을 시작하는 쪽에서 하므로 여기서는 하지 않는다.
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    // 트랜잭션에 바인딩된 Connection은 트랜잭션을 시작한 쪽이 닫으므로 여기서 닫지 않는다.
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    // 공유할 커넥션의 등록과 해제는 트랜잭션을 시작한 쪽에서 담당한다.
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

    // 등록된 커넥션은 유지한다. 트랜잭션 종료 시 등록을 해제한 뒤 호출해야 실제로 닫힌다.
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null || connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

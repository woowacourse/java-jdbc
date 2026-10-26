package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        return TransactionSynchronizationManager.findConnection(dataSource)
                .orElseGet(() -> {
                    try {
                        Connection created = dataSource.getConnection();
                        TransactionSynchronizationManager.bindResource(dataSource, created);
                        return created;
                    } catch (SQLException e) {
                        throw new CannotGetJdbcConnectionException("JDBC 커넥션을 얻는 데 실패했습니다.", e);
                    }
                });
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("JDBC 커넥션을 닫는 데 실패했습니다.", e);
        }
    }
}

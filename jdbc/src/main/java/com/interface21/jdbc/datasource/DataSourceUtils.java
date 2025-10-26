package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            return connection;
        } catch (SQLException ex) {
            log.error("Failed to obtain JDBC Connection: {}", ex.getMessage(), ex);
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        Connection syncedConnection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection == syncedConnection) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            log.error("Failed to close JDBC Connection: {}", ex.getMessage(), ex);
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class.getName());

    private DataSourceUtils() {}

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            log.info("존재하는 Connection 반환 - {}", connection);
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            log.info("새로 만든 Connection 반환 - {}", connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            connection.close();
            TransactionSynchronizationManager.unbindResource(dataSource);
            log.info("Connection 종료 - {}", connection);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

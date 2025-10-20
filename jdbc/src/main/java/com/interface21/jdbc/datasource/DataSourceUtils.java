package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
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
            checkAutoCommitStatus(connection);
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        try {
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (final Exception e) {
            log.warn("unbind 실패", e);
        }

        try {
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }

    private static void checkAutoCommitStatus(final Connection connection) {
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

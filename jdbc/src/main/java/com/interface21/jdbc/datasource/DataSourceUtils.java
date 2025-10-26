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
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (TransactionSynchronizationManager.getResource(dataSource) != connection) {
            closeConnection(connection);
            return;
        }
        try {
            if (!connection.getAutoCommit()) {
                return;
            }
        } catch (SQLException e) {
            log.warn("autoCommit 상태 조회 실패");
        }
        Connection unboundConnection = TransactionSynchronizationManager.unbindResource(dataSource);
        closeConnection(unboundConnection);
    }

    private static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.warn("autoCommit 상태 변경 실패");
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}

package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource)
            throws CannotGetJdbcConnectionException, SQLException {
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
        if (TransactionSynchronizationManager.hasResource(dataSource)) {
            return;
        }
        try {
            connection.close();
            Connection unboundResource = TransactionSynchronizationManager.unbindResource(dataSource);
            log.info("Connection closed for DataSource: {} Unbound Resource: {}", dataSource, unboundResource);
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close Connection");
        }
    }
}


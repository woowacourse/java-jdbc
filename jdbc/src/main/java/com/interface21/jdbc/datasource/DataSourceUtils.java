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

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection heldConnection = (Connection) TransactionSynchronizationManager.getResource(dataSource);
        if (heldConnection != null) {
            return heldConnection;
        }

        Connection connection = fetchConnection(dataSource);
        if (TransactionSynchronizationManager.isTransactionActive()) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
        }
        return connection;
    }

    private static Connection fetchConnection(DataSource dataSource) {
        log.debug("Fetching JDBC Connection from DataSource");
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        Connection heldConnection = (Connection) TransactionSynchronizationManager.getResource(dataSource);
        if (heldConnection != null && heldConnection.equals(connection)) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

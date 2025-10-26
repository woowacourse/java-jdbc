package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) {
        final var connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (TransactionSynchronizationManager.hasResource(dataSource)) {
            return;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException e) {
                log.debug("Could not close JDBC Connection", e);
            }
        }
    }
}

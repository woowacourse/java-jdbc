package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static Connection getConnection(final DataSource dataSource) {
        final var connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static ConnectionResource getConnectionResource(final DataSource dataSource) {
        return new ConnectionResource(getConnection(dataSource), dataSource);
    }

    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (connection == null || connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", e);
        }
    }

    public static final class ConnectionResource implements AutoCloseable {

        private final Connection connection;
        private final DataSource dataSource;

        private ConnectionResource(final Connection connection, final DataSource dataSource) {
            this.connection = connection;
            this.dataSource = dataSource;
        }

        public Connection connection() {
            return connection;
        }

        @Override
        public void close() {
            releaseConnection(connection, dataSource);
        }
    }

}

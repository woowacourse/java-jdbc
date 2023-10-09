package org.springframework.jdbc.datasource;

import static java.util.Objects.isNull;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.ConnectionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    public static ConnectionManager getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final ConnectionManager manager = TransactionSynchronizationManager.getResource(dataSource);
        if (!isNull(manager)) {
            return manager;
        }

        try {
            final Connection connection = dataSource.getConnection();
            final ConnectionManager connectionManager = new ConnectionManager(connection);
            TransactionSynchronizationManager.bindResource(dataSource, connectionManager);
            return connectionManager;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    public static void releaseConnection(final ConnectionManager connectionManager,
                                         final DataSource dataSource
    ) {
        try {
            if (connectionManager.isTransaction()) {
                return;
            }
            final ConnectionManager releasedManager = TransactionSynchronizationManager.unbindResource(dataSource);
            if (connectionManager.hasSameConnection(releasedManager)) {
                connectionManager.close();
            }
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}

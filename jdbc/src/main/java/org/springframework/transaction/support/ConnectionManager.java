package org.springframework.transaction.support;

import static java.lang.Boolean.TRUE;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.transaction.exception.ConnectionManagerException;

public class ConnectionManager {

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    private final ThreadLocal<Connection> connectionResource;
    private final ThreadLocal<Boolean> transactionStarted;

    private ConnectionManager() {
        connectionResource = new ThreadLocal<>();
        transactionStarted = new ThreadLocal<>();
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public void beginTransaction() {
        transactionStarted.set(TRUE);
    }

    public Connection getConnection(final DataSource dataSource) throws SQLException {
        if (isNotAlreadyStarted()) {
            final Connection connection = dataSource.getConnection();
            connectionResource.set(connection);
        }

        final Connection connection = connectionResource.get();

        if (transactionStarted.get() != null && transactionStarted.get()) {
            connection.setAutoCommit(false);
        }

        return connection;
    }

    private boolean isNotAlreadyStarted() {
        return connectionResource.get() == null;
    }

    public void close() {
        final Connection connection = connectionResource.get();

        try {
            connection.close();
        } catch (SQLException e) {
            throw new ConnectionManagerException(e);
        } finally {
            connectionResource.remove();
        }
    }

    public void rollback() {
        try {
            connectionResource.get().rollback();
        } catch (final SQLException e) {
            throw new ConnectionManagerException(e);
        }
    }

    public void commit() {
        try {
            connectionResource.get().commit();
        } catch (final SQLException e) {
            throw new ConnectionManagerException(e);
        }
    }
}

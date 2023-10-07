package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.DataNotFoundException;
import org.springframework.transaction.exception.ConnectionManagerException;

public class ConnectionManager {

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    private final ThreadLocal<Connection> connectionResource;
    private final ThreadLocal<Boolean> startTransactioned;

    private ConnectionManager() {
        connectionResource = new ThreadLocal<>();
        startTransactioned = new ThreadLocal<>();
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public void beginTransaction() {
        startTransactioned.set(Boolean.TRUE);
    }

    public Connection getConnection(final DataSource dataSource) {
        try {
            if (isNotAlreadyStarted()) {
                final Connection connection = dataSource.getConnection();
                beginTransaction(connection);
                connectionResource.set(connection);
            }

            return connectionResource.get();
        } catch (final SQLException e) {
            throw new DataNotFoundException();
        }
    }

    private void beginTransaction(final Connection connection) throws SQLException {
        if (startTransactioned.get() != null && startTransactioned.get()) {
            connection.setAutoCommit(false);
        }
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
            startTransactioned.remove();
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

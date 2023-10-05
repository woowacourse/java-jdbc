package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class ConnectionManager {

    private final ConnectionHolder connectionHolder;
    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this(ConnectionHolder.getInstance(), dataSource);
    }

    ConnectionManager(final ConnectionHolder connectionHolder, final DataSource dataSource) {
        this.connectionHolder = connectionHolder;
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        if (connectionHolder.isEmpty()) {
            return createConnection();
        }
        return connectionHolder.getConnection();
    }

    private Connection createConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection initializeConnection() {
        Connection connection = createConnection();
        connectionHolder.setConnection(connection);
        return connection;
    }

    public void release(final Connection connection) {
        if (connectionHolder.isSameConnection(connection)) {
            return;
        }
        close(connection);
    }

    public void close(final Connection connection) {
        if (connection == null) {
            return;
        }
        if (connectionHolder.isSameConnection(connection)) {
            connectionHolder.clear();
        }
        try {
            connection.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

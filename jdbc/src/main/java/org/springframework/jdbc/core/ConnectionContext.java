package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.ConnectionContextException;

public final class ConnectionContext {

    private static final ThreadLocal<Connection> context = new ThreadLocal<>();

    public static Connection findConnection(final DataSource dataSource) {
        final Connection oldConnection = context.get();

        if (oldConnection != null) {
            return oldConnection;
        }

        try {
            final Connection newConnection = dataSource.getConnection();

            context.set(newConnection);
            return newConnection;
        } catch (final SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    public static void setAutoCommit(final Connection connection, final boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    public static void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (final SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    public static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    public static void remove() {
        try {
            final Connection connection = context.get();

            closeAndRemove(connection);
        } catch (SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    public static void removeNonTransaction() {
        try {
            final Connection connection = context.get();

            closeAndRemove(connection);
        } catch (final SQLException e) {
            throw new ConnectionContextException(e);
        }
    }

    private static void closeAndRemove(final Connection connection) throws SQLException {
        if (connection.getAutoCommit()) {
            connection.close();
            context.remove();
        }
    }

    private ConnectionContext() {
    }
}

package org.springframework.transaction.support;

import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources;

    static {
        resources = new ThreadLocal<>();
        resources.set(new HashMap<>());
    }

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(final DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(final DataSource key, final Connection value) {
        final Map<DataSource, Connection> connections = resources.get();
        try {
            value.setAutoCommit(false);
            connections.put(key, value);
        } catch (final SQLException e) {
            throw new TransactionException("Failed to bind");
        }
    }

    public static void unbindResource(final DataSource key) {
        final Map<DataSource, Connection> connections = resources.get();

        if (connections.containsKey(key)) {
            try {
                final Connection removedConnection = connections.remove(key);
                removedConnection.setAutoCommit(true);
            } catch (final SQLException e) {
                throw new TransactionException("Failed to unbind");
            }
        }
    }
}

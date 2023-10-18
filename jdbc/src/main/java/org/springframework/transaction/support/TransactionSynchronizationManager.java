package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.ConnectionCallBack;
import org.springframework.jdbc.support.exception.CannotBeginTransactionException;
import org.springframework.jdbc.support.exception.CannotCommitException;
import org.springframework.jdbc.support.exception.CannotGetConnectionException;
import org.springframework.jdbc.support.exception.CannotRollbackException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> connections = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isTransactionActive = ThreadLocal.withInitial(() -> false);

    private TransactionSynchronizationManager() {
    }

    public static void beginTransaction() {
        isTransactionActive.set(true);
    }

    public static boolean isTransactionBegan() {
        return isTransactionActive.get();
    }

    public static Connection getResource(final DataSource dataSource) {
        if (isTransactionActive.get()) {
            final Connection connection = guaranteeConnection(dataSource);
            beginTransaction(connection);
            return connection;
        }

        return getConnection(dataSource);
    }

    private static Connection guaranteeConnection(final DataSource dataSource) {
        if (!connections.get().containsKey(dataSource)) {
            Connection connection = getConnection(dataSource);
            connections.get().put(dataSource, connection);
            return connection;
        }

        return connections.get().get(dataSource);
    }

    private static Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetConnectionException(e.getMessage());
        }
    }

    private static void beginTransaction(final Connection connection) {
        action(() -> connection.setAutoCommit(false),
                e -> new CannotBeginTransactionException(e.getMessage())
        );
    }

    private static void action(
            final ConnectionCallBack callBack,
            final Function<SQLException, DataAccessException> exceptionSupplier
    ) {
        try {
            callBack.action();
        } catch (SQLException e) {
            throw exceptionSupplier.apply(e);
        }
    }

    public static void commit(final DataSource dataSource) {
        action(() -> {
            if (connections.get().containsKey(dataSource)) {
                connections.get().get(dataSource).commit();
                clear(dataSource);
            }
        }, e -> new CannotCommitException(e.getMessage()));
    }

    public static void rollback() {
        action(() -> {
            for (final Connection value : connections.get().values()) {
                value.rollback();
            }
            clear();
        }, e -> new CannotRollbackException(e.getMessage()));
    }

    private static void clear(final DataSource dataSource) {
        action(() -> {
            final Connection connection = connections.get().remove(dataSource);
            clearConnection(connection);
        }, e -> new DataAccessException(e.getMessage()));
        isTransactionActive.set(false);
    }

    private static void clearConnection(final Connection connection) {
        action(connection::close, e -> new DataAccessException(e.getMessage()));
    }

    private static void clear() {
        connections.get()
                .values()
                .forEach(TransactionSynchronizationManager::clearConnection);
        connections.get().clear();
        isTransactionActive.set(false);
    }
}

package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.exception.CannotBeginTransactionException;
import org.springframework.jdbc.support.exception.CannotCommitException;
import org.springframework.jdbc.support.exception.CannotGetConnectionException;
import org.springframework.jdbc.support.exception.CannotRollbackException;

public class TransactionManager {

    private static final ThreadLocal<Map<DataSource, Connection>> connections = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Boolean> isTransactionActive = ThreadLocal.withInitial(() -> false);

    public static void beginTransaction() {
        isTransactionActive.set(true);
    }

    public static ConnectionHolder getConnectionHolder(final DataSource dataSource) {
        if (isTransactionActive.get()) {
            final Connection connection = guaranteeConnection(dataSource);
            beginTransaction(connection);
            return ConnectionHolder.activeTransaction(connection);
        }

        return ConnectionHolder.disableTransaction(getConnection(dataSource));
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

    private static void beginTransaction(final Connection connection) {
        action(() -> connection.setAutoCommit(false),
                e -> new CannotBeginTransactionException(e.getMessage())
        );
    }

    public static void commit(final DataSource dataSource) {
        action(() -> {
            if (connections.get().containsKey(dataSource)) {
                connections.get().get(dataSource).commit();
            }

            clear(dataSource);
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
            connection.close();
        }, e -> new DataAccessException(e.getMessage()));
    }

    private static void clear() {
        connections.get().clear();
    }
}

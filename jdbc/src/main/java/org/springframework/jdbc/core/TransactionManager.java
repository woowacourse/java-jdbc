package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final ConnectionManager connectionManager;

    public TransactionManager(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public <T> T save(final BiConsumer<Connection, T> consumer, final T entity) {
        final var connection = connectionManager.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            consumer.accept(connection, entity);
            connection.commit();
            connection.setAutoCommit(true);
            return entity;
        } catch (Exception exception) {
            try {
                connection.rollback();
                throw new DataAccessException(exception);
            } catch (SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
        }
    }

    public <T> T find(final BiFunction<Connection, Object[], T> function, final Object... parameters) {
        final var connection = connectionManager.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            final var result = function.apply(connection, parameters);
            connection.commit();
            connection.setAutoCommit(true);
            return result;
        } catch (Exception exception) {
            try {
                connection.rollback();
                throw new DataAccessException(exception);
            } catch (SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
        }
    }
}

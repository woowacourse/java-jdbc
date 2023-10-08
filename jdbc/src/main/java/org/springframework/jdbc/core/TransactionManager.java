package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class TransactionManager {

    private final Logger log = LoggerFactory.getLogger(TransactionManager.class);
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
            return entity;
        } catch (Exception exception) {
            try {
                connection.rollback();
                throw new DataAccessException(exception);
            } catch (SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
                log.warn("fail to set auto commit true due to {}", ignored.getMessage());
                log.warn(Arrays.toString(ignored.getStackTrace()));
            }
        }
    }

    public <T> T find(final BiFunction<Connection, Object[], T> function, final Object... parameters) {
        final var connection = connectionManager.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            final var result = function.apply(connection, parameters);
            connection.commit();
            return result;
        } catch (Exception exception) {
            try {
                connection.rollback();
                throw new DataAccessException(exception);
            } catch (SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
                log.warn("fail to set auto commit true due to {}", ignored.getMessage());
                log.warn(Arrays.toString(ignored.getStackTrace()));
            }
        }
    }
}

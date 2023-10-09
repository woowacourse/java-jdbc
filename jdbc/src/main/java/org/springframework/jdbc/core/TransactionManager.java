package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final Logger log = LoggerFactory.getLogger(TransactionManager.class);
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T save(final BiConsumer<Connection, T> consumer, final T entity) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
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
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (SQLException ignored) {
                log.warn("fail to set auto commit true due to {}", ignored.getMessage());
                log.warn(Arrays.toString(ignored.getStackTrace()));
            }
        }
    }

    public <T> T find(final BiFunction<Connection, Object[], T> function, Object... parameters) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
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
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (SQLException ignored) {
                log.warn("fail to set auto commit true due to {}", ignored.getMessage());
                log.warn(Arrays.toString(ignored.getStackTrace()));
            }
        }
    }

}

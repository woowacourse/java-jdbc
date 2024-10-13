package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeInTransaction(Consumer<Connection> consumer) {
        try (Connection connection = dataSource.getConnection()) {
            executeInTransaction(connection, consumer);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void executeInTransaction(Connection connection, Consumer<Connection> consumer) throws SQLException {
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
        } catch (DataAccessException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    public <T> T getResultInTransaction(Function<Connection, T> function) {
        try (Connection connection = dataSource.getConnection()) {
            return getResultInTransaction(connection, function);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private  <T> T getResultInTransaction(Connection connection, Function<Connection, T> function) throws SQLException {
        try {
            connection.setAutoCommit(false);
            return function.apply(connection);
        } catch (DataAccessException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }
}

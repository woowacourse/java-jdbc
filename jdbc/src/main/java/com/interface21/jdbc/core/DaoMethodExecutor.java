package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class DaoMethodExecutor {

    public static void executeConsumerInTx(Connection connection, Consumer<Connection> consumer) {
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            close(connection);
        }
    }

    private static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }

    private static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }

    public static void executeConsumer(Connection connection, Consumer<Connection> consumer) {
        try (connection) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static <T> T executeFunction(Connection connection, Function<Connection, T> function) {
        try (connection) {
            return function.apply(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

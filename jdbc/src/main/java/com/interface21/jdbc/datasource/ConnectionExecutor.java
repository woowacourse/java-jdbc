package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class ConnectionExecutor {

    private ConnectionExecutor() {

    }

    public static void executeTransactional(DataSource dataSource, Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            close(connection, dataSource);
        }
    }

    private static void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static void close(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        DataSourceUtils.releaseConnection(connection, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    public static void execute(DataSource dataSource, Consumer<Connection> consumer) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public static <T> T apply(DataSource dataSource, Function<Connection, T> function) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            return function.apply(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

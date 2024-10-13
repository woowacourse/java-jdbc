package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class ConnectionExecutor {

    private ConnectionExecutor() {

    }

    public static void executeTransactional(DataSource dataSource, Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            runnable.run();
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
    }

    public static void execute(DataSource dataSource, Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        runnable.run();
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public static <T> T supply(DataSource dataSource, Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        T result = supplier.get();
        DataSourceUtils.releaseConnection(connection, dataSource);
        return result;
    }
}

package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TxManager {

    private TxManager() {
    }

    public static <T> T run(Function<Connection, T> function) {
        DataSource dataSource = DataSourceConfig.getInstance();
        try (Connection connection = dataSource.getConnection()) {
            return doRun(function, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T doRun(Function<Connection, T> function, Connection connection) throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = function.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(e);
        }
    }

    public static void run(Consumer<Connection> consumer) {
        run(connection -> {
            consumer.accept(connection);
            return null;
        });
    }
}

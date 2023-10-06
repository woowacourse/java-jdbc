package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.dao.DataAccessException;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void transactionCommand(Consumer<Connection> consumer) {
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            consumer.accept(connection);

            connection.commit();
        } catch (SQLException firstException) {
            try {
                connection.rollback();
            } catch (SQLException secondException) {
                throw new DataAccessException(secondException);
            }

            throw new DataAccessException(firstException);
        }
    }

    public static <T> T transactionQuery(Function<Connection, T> function) {
        try {
            Connection connection = getConnection();

            return function.apply(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DataSourceConfig.getInstance()
                .getConnection();
    }

}

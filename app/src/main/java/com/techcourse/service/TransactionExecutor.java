package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void transactionCommand(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
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
        } finally {
            DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
        }
    }

    public static <T> T transactionQuery(Function<Connection, T> function) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());

        return function.apply(connection);
    }

}

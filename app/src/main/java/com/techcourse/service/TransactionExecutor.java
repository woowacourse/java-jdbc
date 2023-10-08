package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private TransactionExecutor() {
    }

    public static void transactionCommand(Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            connection.setAutoCommit(false);

            runnable.run();

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

    public static <T> T transactionQuery(Supplier<T> supplier) {
        try {
            DataSourceUtils.getConnection(DataSourceConfig.getInstance());
            return supplier.get();
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(DataSourceConfig.getInstance());
        }
    }

}

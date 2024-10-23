package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.DataQueryException;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionExecutor {


    public static <T> T doTransaction(TransactionWork<T> work) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);

            T result = work.execute();

            connection.commit();
            return result;
        } catch (Exception e) {
            handleException(connection);
            if (e instanceof SQLException) {
                throw new DataQueryException(e.getMessage(), e);
            }
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private static void handleException(Connection connection) {
        if (connection != null) {
            handleRollBack(connection);
        }
    }

    private static void handleRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            throw new DataQueryException(rollbackEx.getMessage(), rollbackEx);
        }
    }
}

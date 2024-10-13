package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallback<T> action) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            T result = action.doInTransaction(connection);

            connection.commit();
            return result;
        } catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    throw new RuntimeException("Failed to rollback transaction", rollbackException);
                }
            }
            throw new DataAccessException(exception.getMessage(), exception);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void executeWithoutResult(Consumer<Connection> action) {
        execute(connection -> {
            action.accept(connection);
            return null;
        });
    }
}

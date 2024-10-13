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

    public void executeWithoutResult(Consumer<Connection> action) {
        execute(connection -> {
            action.accept(connection);
            return null;
        });
    }

    public <T> T execute(TransactionCallback<T> action) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            return transaction(connection, action);
        } catch (SQLException exception) {
            rollbackOnException(connection);
            throw new DataAccessException(exception.getMessage(), exception);
        } finally {
            processConnectionClose(connection);
        }
    }

    private <T> T transaction(Connection connection, TransactionCallback<T> action) throws SQLException {
        connection.setAutoCommit(false);

        T result = action.doInTransaction(connection);

        connection.commit();
        return result;
    }

    private void rollbackOnException(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                throw new DataAccessException("Failed to rollback transaction", rollbackException);
            }
        }
    }

    private void processConnectionClose(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}

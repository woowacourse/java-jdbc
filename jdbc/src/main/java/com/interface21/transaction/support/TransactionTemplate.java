package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            return transaction(connection, action);
        } catch (Exception exception) {
            rollbackOnException(connection);
            throw new DataAccessException(exception.getMessage(), exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private <T> T transaction(Connection connection, TransactionCallback<T> action) throws SQLException {
        connection.setAutoCommit(false);

        T result = action.doInTransaction(connection);

        connection.commit();
        return result;
    }

    private void rollbackOnException(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            throw new DataAccessException("Failed to rollback transaction", rollbackException);
        }
    }
}

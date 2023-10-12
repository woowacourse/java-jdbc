package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final Supplier<T> supplier) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            DataSourceUtils.startTransaction(connection, dataSource);
            final T t = supplier.get();
            DataSourceUtils.finishTransaction(connection, dataSource);
            return t;
        } catch (DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void execute(final Runnable runnable) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            DataSourceUtils.startTransaction(connection, dataSource);
            runnable.run();
            DataSourceUtils.finishTransaction(connection, dataSource);
        } catch (DataAccessException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }
}

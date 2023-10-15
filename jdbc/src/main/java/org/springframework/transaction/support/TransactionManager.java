package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable commandExecutor) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            commandExecutor.run();

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            release(connection);
        }
    }

    public <T> T query(Supplier<T> queryExecutor) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setReadOnly(true);

            connection.setAutoCommit(false);
            T result = queryExecutor.get();

            connection.commit();
            return result;
        } catch (Exception e) {
            rollback(connection);
            throw new TransactionException(e);
        } finally {
            release(connection);
        }
    }

    private void rollback(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException(e);
        }
    }

    private void release(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.setAutoCommit(true);

            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        } catch (Exception e) {
            throw new TransactionException(e);
        }
    }

}

package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransactionManager implements TransactionManager {

    @Override
    public void execute(DataSource dataSource, TransactionExecutor method) {
        final var connection = TransactionManager.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            method.execute();
            connection.commit();
        } catch (RuntimeException | SQLException e) {
            handleTransactionException(connection, e);
        } finally {
            cleanUpTransaction(dataSource, connection);
        }
    }

    @Override
    public <T> T executeAndReturn(DataSource dataSource, TransactionSupplier<T> method) {
        final var connection = TransactionManager.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = method.get();
            connection.commit();
            return result;
        } catch (RuntimeException | SQLException e) {
            handleTransactionException(connection, e);
            return null;
        } finally {
            cleanUpTransaction(dataSource, connection);
        }
    }

    private static void handleTransactionException(Connection connection, Exception e) {
        try {
            connection.rollback();
            throw new DataAccessException(e);
        } catch (SQLException rollbackException) {
            throw new RuntimeException(rollbackException);
        }
    }

    private void cleanUpTransaction(DataSource dataSource, Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}

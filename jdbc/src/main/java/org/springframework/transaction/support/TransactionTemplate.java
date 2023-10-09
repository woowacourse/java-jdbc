package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.ConnectionException;
import org.springframework.jdbc.exception.RollbackFailException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeWithTransaction(TransactionExecutor<T> transactionExecutor) {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.setActualTransactionActive(true);
            T result = transactionExecutor.execute(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        } catch (DataAccessException e) {
            rollback(connection);
            throw e;
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RollbackFailException(e.getMessage());
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.setActualTransactionActive(false);
                TransactionSynchronizationManager.unbindResource(dataSource);
            } catch (SQLException e) {
                throw new ConnectionException(e.getMessage());
            }
        }
    }
}

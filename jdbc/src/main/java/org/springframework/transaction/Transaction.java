package org.springframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private static final Logger log = LoggerFactory.getLogger(Transaction.class);

    private final DataSource dataSource;

    public Transaction(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T run(final TransactionTemplate<T> transactionTemplate) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        start(connection);
        try {
            final T result = transactionTemplate.execute(connection);

            commit(connection);
            return result;
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void start(final Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(final Connection connection) throws SQLException {
        connection.commit();
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}

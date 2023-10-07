package org.springframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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
        final Connection connection = initializeConnection();
        try {
            final T result = transactionTemplate.execute(connection);

            commit(connection);

            return result;
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        } finally {
            close(connection);
        }
    }

    private Connection initializeConnection() {
        try {
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
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

    private void close(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}

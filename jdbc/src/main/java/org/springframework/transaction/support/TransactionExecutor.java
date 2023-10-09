package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final TransactionExecuteStrategy<T> executeStrategy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            final T result = executeStrategy.strategy();

            connection.commit();

            return result;
        } catch (Exception e) {
            log.error("execute exception : {}", e);
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error("rollback exception : {}", e);
            throw new DataAccessException(e);
        }
    }
}

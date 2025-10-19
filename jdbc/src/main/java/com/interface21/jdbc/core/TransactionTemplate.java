package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void doInTransaction(final Runnable execution) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            execution.run();

            connection.commit();
        } catch (final SQLException | RuntimeException e) {
            throw rollbackAndThrow(e, connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T returnInTransaction(final Supplier<T> execution) {
        final Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);

            T result = execution.get();

            connection.commit();

            return result;
        } catch (final SQLException | DataAccessException e) {
            throw rollbackAndThrow(e, connection);
        } finally {
            close(connection);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException(e.getMessage(), e);
        }
    }

    private RuntimeException rollbackAndThrow(final Exception jdbcEx, final Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (final SQLException rollbackEx) {
            jdbcEx.addSuppressed(rollbackEx);
        }
        log.error(jdbcEx.getMessage(), jdbcEx);
        return new DataAccessException(jdbcEx);
    }

    private void close(final Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (final SQLException ignored) {}
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.error.SqlExceptionConverter;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    private TransactionTemplate() {
    }

    public static void executeWithoutReturn(final Runnable transactionCallback, final DataSource dataSource) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            transactionCallback.run();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public static <T> T ExecuteWithReturn(final Supplier<T> transactionCallback, final DataSource dataSource) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            return transactionCallback.get();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw SqlExceptionConverter.convert(e);
        }
    }
}

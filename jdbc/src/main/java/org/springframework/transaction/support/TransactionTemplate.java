package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionTemplate {

    public static <T> T execute(DataSource dataSource, TransactionExecution<T> action) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = action.execute();
            connection.commit();
            return result;
        } catch (final Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            setAutoCommitFalse(connection);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }


    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    private static void setAutoCommitFalse(final Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

}

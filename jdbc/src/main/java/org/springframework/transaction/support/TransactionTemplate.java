package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionTemplate {

    private TransactionTemplate() {
    }

    public static void execute(Runnable transactionCallback, DataSource dataSource) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            transactionCallback.run();

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
                throw new DataAccessException(e);
            } catch (SQLException e2) {
                throw new DataAccessException(e2);
            }
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    public static <T> T query(Supplier<T> transactionCallback, DataSource dataSource) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);

            return transactionCallback.get();
        } catch (SQLException e) {
                throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable runnable) {
        executeWithResult(() -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithResult(final Supplier<T> supplier) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try{
            connection.setAutoCommit(false);
            final T result = supplier.get();
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

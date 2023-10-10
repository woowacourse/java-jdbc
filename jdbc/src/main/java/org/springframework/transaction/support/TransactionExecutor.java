package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionExecutor implements TransactionManager {

    @Override
    public <T> T execute(DataSource dataSource, Supplier<T> method) {
        final var connection = TransactionManager.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            T result = method.get();
            connection.commit();
            return result;
        } catch (RuntimeException | SQLException e) {
            try {
                connection.rollback();
                throw new DataAccessException(e);
            } catch (SQLException rollbackException) {
                throw new RuntimeException(rollbackException);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}

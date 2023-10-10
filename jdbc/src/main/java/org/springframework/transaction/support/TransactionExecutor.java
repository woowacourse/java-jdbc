package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionExecutor implements TransactionManager {

    @Override
    public void execute(DataSource dataSource, Consumer<Void> consumer) {
        final var connection = TransactionManager.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            consumer.accept(null);
            connection.commit();
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

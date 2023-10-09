package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionExecutor implements TransactionManager {

    @Override
    public void execute(DataSource dataSource, Consumer<Connection> consumer) {
        final var connection = TransactionManager.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (RuntimeException | SQLException e) {
            try {
                connection.rollback();
                throw new DataAccessException(e);
            } catch (SQLException rollbackException) {
                throw new RuntimeException(rollbackException);
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException closeException) {
                throw new RuntimeException(closeException);
            }
        }
    }
}

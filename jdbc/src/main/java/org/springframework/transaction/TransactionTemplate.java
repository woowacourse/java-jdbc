package org.springframework.transaction;

import org.springframework.connection.ConnectionManager;
import org.springframework.dao.DataAccessException;
import java.sql.Connection;
import java.util.function.Supplier;

public class TransactionTemplate {

    private final ConnectionManager connectionManager;

    public TransactionTemplate(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public <T> T executeWithTransaction(final Supplier<T> service) {
        final Connection connection = connectionManager.getConnection(false);
        try {
            final T result = service.get();
            connection.commit();
            return result;
        } catch (final Exception exception) {
            connectionManager.rollback(connection);
            throw new DataAccessException(exception.getMessage());
        } finally {
            connectionManager.close(connection);
        }
    }
}

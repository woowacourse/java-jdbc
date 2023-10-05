package org.springframework.transaction;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionContext;
import org.springframework.transaction.exception.TransactionTemplateException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T executeQueryWithTransaction(final TransactionalCallbackWithReturnValue<T> callback) {
        final Connection connection = ConnectionContext.findConnection(dataSource);

        try {
            ConnectionContext.setAutoCommit(connection, false);

            final T result = callback.execute();

            ConnectionContext.commit(connection);
            return result;
        } catch (final DataAccessException e) {
            ConnectionContext.rollback(connection);

            throw new TransactionTemplateException(e);
        } finally {
            ConnectionContext.remove();
        }
    }

    public void executeQueryWithTransaction(final TransactionalCallbackWithoutReturnValue callback) {
        final Connection connection = ConnectionContext.findConnection(dataSource);

        try {
            ConnectionContext.setAutoCommit(connection, false);
            callback.execute();
            ConnectionContext.commit(connection);
        } catch (final RuntimeException e) {
            ConnectionContext.rollback(connection);

            throw new TransactionTemplateException(e);
        } finally {
            ConnectionContext.remove();
        }
    }
}

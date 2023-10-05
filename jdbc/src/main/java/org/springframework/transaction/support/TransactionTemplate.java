package org.springframework.transaction.support;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class TransactionTemplate {

    private final ConnectionManager connectionManager;

    public TransactionTemplate(final DataSource dataSource) {
        this(new ConnectionManager(dataSource));
    }

    public TransactionTemplate(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void execute(final TransactionCallback transactionCallback) {
        Connection connection = null;
        try {
            connection = connectionManager.initializeConnection();
            connection.setAutoCommit(false);
            transactionCallback.execute();
            connection.commit();
        } catch (final Exception e) {
            connectionManager.rollback(connection);
            throw new DataAccessException(e);
        } finally {
            connectionManager.close(connection);
        }
    }
}

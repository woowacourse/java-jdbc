package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionManager;

public class TransactionExecutor {

    private final ConnectionManager connectionManager;

    public TransactionExecutor(final ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void execute(final TransactionWorker worker) {
        final Connection connection = connectionManager.getConnection();
        try {
            connection.setAutoCommit(false);

            worker.run(connection);

            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    private void rollback(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}

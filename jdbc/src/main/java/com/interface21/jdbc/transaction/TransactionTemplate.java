package com.interface21.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);
    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final TransactionCallback callback) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            ConnectionHolder.setConnection(connection);
            connection.setAutoCommit(false);

            callback.execute();

            connection.commit();
        } catch (Exception e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private void rollback(Exception e, Connection connectionToRollback) {
        if (connectionToRollback != null) {
            try {
                connectionToRollback.rollback();
                log.error("Transaction rolled back due to: ", e);
            } catch (SQLException rollbackException) {
                log.error("Failed to rollback transaction", rollbackException);
                e.addSuppressed(rollbackException);
            }
        }
    }

    private void closeConnection(Connection connectionToClose) {
        ConnectionHolder.clear();
        if (connectionToClose != null) {
            try {
                connectionToClose.close();
            } catch (SQLException closeException) {
                log.error("Failed to close connection", closeException);
            }
        }
    }
}

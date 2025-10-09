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
        try {
            Connection connection = dataSource.getConnection();
            ConnectionHolder.setConnection(connection);
            connection.setAutoCommit(false);

            callback.execute();

            connection.commit();
        } catch (Exception e) {
            Connection connectionToRollback = ConnectionHolder.getConnection();
            rollback(e, connectionToRollback);
        } finally {
            Connection connectionToClose = ConnectionHolder.getConnection();
            closeConnection(connectionToClose);
        }
    }

    private void rollback(Exception e, Connection connectionToRollback) {
        if (connectionToRollback != null) {
            try {
                connectionToRollback.rollback();
            } catch (SQLException ex) {
                log.error("Rollback failed: {}", ex.getMessage());
            }
        }
        throw new DataAccessException(e);
    }

    private void closeConnection(Connection connectionToClose) {
        if (connectionToClose != null) {
            ConnectionHolder.clear();
            try {
                connectionToClose.close();
            } catch (SQLException ex) {
                log.error("close connection failed: {}", ex.getMessage());
            }
        }
    }
}

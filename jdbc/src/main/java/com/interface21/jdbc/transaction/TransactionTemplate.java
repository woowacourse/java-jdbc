package com.interface21.jdbc.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

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
                System.err.println("Rollback failed: " + ex.getMessage());
            }
        }
        throw new RuntimeException(e);
    }

    private void closeConnection(Connection connectionToClose) {
        if (connectionToClose != null) {
            ConnectionHolder.setConnection(null);
            try {
                connectionToClose.close();
            } catch (SQLException ex) {
                System.err.println("Connection close failed: " + ex.getMessage());
            }
        }
    }
}

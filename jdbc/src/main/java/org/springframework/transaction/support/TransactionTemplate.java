package org.springframework.transaction.support;

import org.springframework.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable runnable) {
        Connection connection = begin();
        try {
            runnable.run();
            commit(connection);
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private Connection begin() {
        try {
            TransactionSynchronizationManager.setInTransaction(true);
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindConnection(dataSource, connection);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void commit(Connection connection) {
        try {
            connection.commit();
            release(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            release(connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void release(Connection connection) {
        try {
            connection.close();
            TransactionSynchronizationManager.setInTransaction(false);
            TransactionSynchronizationManager.unbindConnection(dataSource);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

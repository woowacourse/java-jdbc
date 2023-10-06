package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

public class TransactionManager {
    private final ConnectionAgent connectionAgent;

    public TransactionManager(final DataSource dataSource) {
        this.connectionAgent = new ConnectionAgent(dataSource);
    }

    public void transact(final Runnable runnable) {
        Connection connection = null;
        try {
            connection = connectionAgent.getConnection();
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch (SQLException exception) {
            rollback(connection);
            throw new DataAccessException();
        } finally {
            close(connection);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException();
        }
    }

    private void close(final Connection connection) {
        try {
            connection.close();
            ConnectionHolder.clean();
        } catch(SQLException exception) {
            throw new DataAccessException();
        }
    }
}

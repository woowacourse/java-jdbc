package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            ConnectionHolder connectionHolder = new ConnectionHolder(connection);
            connectionHolder.setTransaction(true);
            TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
            Connection connection = connectionHolder.getConnection();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            ConnectionHolder connectionHolder = TransactionSynchronizationManager.getResource(dataSource);
            Connection connection = connectionHolder.getConnection();
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

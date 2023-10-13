package org.springframework.transaction.support;

import org.springframework.jdbc.exception.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createConnection() {
        try {
            Connection connection = dataSource.getConnection();
            ConnectionHolder connectionHolder = new ConnectionHolder(connection);
            connection.setAutoCommit(false);
            connectionHolder.setIsTransactionActive(true);
            TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
        } catch (Exception e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    public void commit() {
        TransactionSynchronizationManager.getResource(dataSource).commit();
    }

    public void rollback() {
        TransactionSynchronizationManager.getResource(dataSource).rollback();
    }

    public void close() {
        TransactionSynchronizationManager.getResource(dataSource).close();
        TransactionSynchronizationManager.unbindResource(dataSource);
    }
}

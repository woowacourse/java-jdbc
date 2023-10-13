package org.springframework.transaction.support;

import org.springframework.jdbc.exception.TransactionException;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;
    private boolean isTransactionActive;

    public ConnectionHolder(Connection connection) {
        this.connection = connection;
    }

    public void commit() {
        try {
            connection.commit();
        } catch (Exception e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            throw new TransactionException(e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setIsTransactionActive(boolean isTransactionActive) {
        this.isTransactionActive = isTransactionActive;
    }

    public boolean isTransactionActive() {
        return isTransactionActive;
    }

    public boolean isSameConnection(Connection connection) {
        return this.connection == connection;
    }
}

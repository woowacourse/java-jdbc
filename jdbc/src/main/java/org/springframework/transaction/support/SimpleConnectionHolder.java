package org.springframework.transaction.support;

import java.sql.Connection;

public class SimpleConnectionHolder {

    private final Connection connection;
    private boolean transactionActive;

    public SimpleConnectionHolder(final Connection connection) {
        this(connection, false);
    }

    public SimpleConnectionHolder(final Connection connection, final boolean transactionActive) {
        this.connection = connection;
        this.transactionActive = transactionActive;
    }

    public void setTransactionActive(final boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    public boolean isSameConnection(final Connection connection) {
        return this.connection == connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isTransactionActive() {
        return transactionActive;
    }
}


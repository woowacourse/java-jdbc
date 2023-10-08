package org.springframework.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;
    private boolean transactionActive;

    public ConnectionHolder(final Connection connection) {
        this(connection, false);
    }

    public ConnectionHolder(final Connection connection, final boolean transactionActive) {
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


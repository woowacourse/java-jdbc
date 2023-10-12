package org.springframework.jdbc;

import static java.util.Objects.isNull;

import java.sql.Connection;

public class ConnectionHolder {

    private Connection connection;
    private boolean transactionActive = false;

    public ConnectionHolder(final Connection connection) {
        this.connection = connection;
    }

    public void setTransactionActive(final boolean transactionActive) {
        this.transactionActive = transactionActive;
    }

    public boolean isTransactionActive() {
        return transactionActive;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean has(final Connection connection) {
        return this.connection == connection;
    }
}

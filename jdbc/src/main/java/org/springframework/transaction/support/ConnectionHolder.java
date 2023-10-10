package org.springframework.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;
    private boolean isTransactionActive = false;

    public ConnectionHolder(final Connection connection) {
        this.connection = connection;
    }

    public void setTransactionActive(final boolean isTransactionActive) {
        this.isTransactionActive = isTransactionActive;
    }

    public boolean isTransactionActive() {
        return isTransactionActive;
    }

    public Connection getConnection() {
        return connection;
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;

    private boolean isConnectionTransactionActive;

    public ConnectionHolder(final Connection connection) {
        this.connection = connection;
    }

    public void setConnectionTransactionActive(boolean isActive) {
        this.isConnectionTransactionActive = isActive;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnectionTransactionActive() {
        return isConnectionTransactionActive;
    }
}

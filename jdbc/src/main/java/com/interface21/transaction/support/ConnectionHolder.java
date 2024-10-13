package com.interface21.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;
    private boolean isTransactionActive;

    public ConnectionHolder(Connection connection) {
        this.connection = connection;
        this.isTransactionActive = false;
    }

    public void setTransactionActive(boolean isTransactionActive) {
        this.isTransactionActive = isTransactionActive;
    }

    public boolean isTransactionActive() {
        return isTransactionActive;
    }

    public Connection getConnection() {
        return connection;
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private final Connection connection;
    private boolean isTransaction;

    public ConnectionHolder(Connection connection, boolean isTransaction) {
        this.connection = connection;
        this.isTransaction = isTransaction;
    }

    public ConnectionHolder(Connection connection) {
        this(connection, false);
    }

    public boolean isSameConnection(Connection connection) {
        return this.connection.equals(connection);
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public void setTransaction(boolean isTransaction) {
        this.isTransaction = isTransaction;
    }

    public Connection getConnection() {
        return connection;
    }
}

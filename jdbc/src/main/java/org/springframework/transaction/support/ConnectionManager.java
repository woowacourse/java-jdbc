package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {
    private final Connection connection;

    private boolean isTransaction;

    public ConnectionManager(final Connection connection) {
        this.connection = connection;
    }

    public void activeTransaction() throws SQLException{
        connection.setAutoCommit(false);
        isTransaction = true;
    }

    public boolean hasSameConnection(final ConnectionManager other) {
        return this.connection == other.connection;
    }

    public void inactiveTransaction() {
        isTransaction = false;
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public void close() throws SQLException {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isTransaction() {
        return isTransaction;
    }
}

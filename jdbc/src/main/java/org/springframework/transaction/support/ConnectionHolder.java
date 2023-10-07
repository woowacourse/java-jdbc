package org.springframework.transaction.support;

import java.sql.Connection;

class ConnectionHolder {

    private static final ConnectionHolder INSTANCE = new ConnectionHolder();

    private final ThreadLocal<Connection> connection = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    static ConnectionHolder getInstance() {
        return INSTANCE;
    }

    public void setConnection(final Connection connection) {
        this.connection.set(connection);
    }

    public Connection getConnection() {
        return connection.get();
    }

    public void clear() {
        connection.remove();
    }

    public boolean isEmpty() {
        return connection.get() == null;
    }

    public boolean isSameConnection(final Connection connection) {
        return this.connection.get() == connection;
    }
}

package org.springframework.transaction.support;

import java.sql.Connection;

public class ConnectionHolder {

    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    private ConnectionHolder() {
    }

    public static ConnectionHolder getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public Connection getConnection() {
        return connectionThreadLocal.get();
    }

    public void setConnection(Connection connection) {
        connectionThreadLocal.set(connection);
    }

    public boolean isEmpty() {
        return connectionThreadLocal.get() == null;
    }

    public void remove() {
        connectionThreadLocal.remove();
    }

    private static class SingletonHelper {
        private static final ConnectionHolder INSTANCE = new ConnectionHolder();
    }
}

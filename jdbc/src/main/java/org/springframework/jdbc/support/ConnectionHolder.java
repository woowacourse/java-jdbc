package org.springframework.jdbc.support;

import java.sql.Connection;

public class ConnectionHolder implements AutoCloseable {

    private final Connection connection;
    private final boolean isTransactionActive;

    private ConnectionHolder(final Connection connection, final boolean isTransactionActive) {
        this.connection = connection;
        this.isTransactionActive = isTransactionActive;
    }

    public static ConnectionHolder activeTransaction(final Connection connection) {
        return new ConnectionHolder(connection, true);
    }

    public static ConnectionHolder disableTransaction(final Connection connection) {
        return new ConnectionHolder(connection, false);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (!isTransactionActive) {
            connection.close();
        }
    }
}

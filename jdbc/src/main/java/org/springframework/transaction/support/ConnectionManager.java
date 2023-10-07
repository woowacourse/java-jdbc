package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionManager {

    private final ThreadLocal<Connection> connectionResource;

    public ConnectionManager() {
        connectionResource = new ThreadLocal<>();
    }

    public Connection getConnection(final DataSource dataSource) throws SQLException {
        if (isNotAlreadyStarted()) {
            final Connection connection = dataSource.getConnection();
            connectionResource.set(connection);
        }

        return connectionResource.get();
    }

    private boolean isNotAlreadyStarted() {
        return connectionResource.get() == null;
    }
}

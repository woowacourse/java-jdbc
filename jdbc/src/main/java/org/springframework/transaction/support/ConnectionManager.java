package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionManager {

    private static final ConnectionManager INSTANCE = new ConnectionManager();

    private final ThreadLocal<Connection> connectionResource;

    private ConnectionManager() {
        connectionResource = new ThreadLocal<>();
    }

    public static ConnectionManager getInstance(){
        return INSTANCE;
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

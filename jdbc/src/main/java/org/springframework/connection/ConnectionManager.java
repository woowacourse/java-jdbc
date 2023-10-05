package org.springframework.connection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getAutoCommittedConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getNotAutoCommittedConnection() {
        try {
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(final Connection connection) {
        try {
            connection.close();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

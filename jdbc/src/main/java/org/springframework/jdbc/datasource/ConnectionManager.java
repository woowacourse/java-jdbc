package org.springframework.jdbc.datasource;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void close(final Connection connection) {
        try {
            connection.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

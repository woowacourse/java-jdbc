package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void closeConnection(final Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

package org.springframework.transaction;

import static java.util.Objects.isNull;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

public class ConnectionAgent {

    private final DataSource dataSource;

    public ConnectionAgent(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        Connection connection = ConnectionHolder.getConnection();
        if (isNull(connection)) {
            connection = createConnection();
            ConnectionHolder.setConnection(connection);
        }
        return connection;
    }

    private Connection createConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new DataAccessException();
        }
    }
}

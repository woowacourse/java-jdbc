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

    public Connection getConnectionAndSave() {
        final Connection connection = createConnection();
        ConnectionHolder.setConnection(connection);
        return connection;
    }

    private Connection createConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new DataAccessException();
        }
    }

    public Connection getConnection() {
        Connection connection = ConnectionHolder.getConnection();
        if (isNull(connection)) {
            connection = createConnection();
        }
        return connection;
    }

    public void release(final Connection connection) {
        if(ConnectionHolder.hasSame(connection)) {
            return;
        }
        close(connection);
    }

    public void close(final Connection connection) {
        try {
            connection.close();
            ConnectionHolder.clean();
        } catch(SQLException exception) {
            throw new DataAccessException();
        }
    }
}

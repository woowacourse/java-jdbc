package com.interface21.jdbc.transaction;

import com.interface21.jdbc.exception.JdbcAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionManager {

    private final Map<DataSource, Connection> connections = new HashMap<>();

    public void begin(DataSource dataSource) throws SQLException {
        if (connections.containsKey(dataSource)) {
            throw new JdbcAccessException("Transaction already started for this datasource");
        }

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        connections.put(dataSource, connection);
    }

    public void commit(DataSource dataSource) throws SQLException {
        try (Connection connection = getConnection(dataSource)) {
            connection.commit();
            connections.remove(dataSource);
        }
    }

    public void rollback(DataSource dataSource) throws SQLException {
        try (Connection connection = getConnection(dataSource)) {
            connection.rollback();
            connections.remove(dataSource);
        }
    }

    public Connection getConnection(DataSource dataSource) {
        if (!hasConnection(dataSource)) {
            throw new JdbcAccessException("Connection not found for this datasource");
        }
        return connections.get(dataSource);
    }

    public boolean hasConnection(DataSource dataSource) {
        return connections.containsKey(dataSource);
    }
}

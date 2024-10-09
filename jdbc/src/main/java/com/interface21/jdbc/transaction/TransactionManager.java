package com.interface21.jdbc.transaction;

import com.interface21.jdbc.exception.JdbcAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionManager {

    private static final Map<DataSource, Connection> CONNECTIONS = new HashMap<>();

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() throws SQLException {
        if (CONNECTIONS.containsKey(dataSource)) {
            throw new JdbcAccessException("Transaction already started for this datasource");
        }

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        CONNECTIONS.put(dataSource, connection);
    }

    public void commit() throws SQLException {
        try (Connection connection = getConnection()) {
            connection.commit();
            CONNECTIONS.remove(dataSource);
        }
    }

    public void rollback() throws SQLException {
        try (Connection connection = getConnection()) {
            connection.rollback();
            CONNECTIONS.remove(dataSource);
        }
    }

    public Connection getConnection() {
        if (!hasConnection()) {
            throw new JdbcAccessException("Connection not found for this datasource");
        }
        return CONNECTIONS.get(dataSource);
    }

    public boolean hasConnection() {
        return CONNECTIONS.containsKey(dataSource);
    }
}

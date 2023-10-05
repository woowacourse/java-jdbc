package org.springframework.transaction;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final Connection connection;

    public TransactionManager(DataSource dataSource) {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    public void begin() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public Connection getConnection() {
        return connection;
    }
}

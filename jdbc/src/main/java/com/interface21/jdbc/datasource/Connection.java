package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Connection {
    private final java.sql.Connection connection;

    public Connection(java.sql.Connection connection) {
        this.connection = connection;
    }

    public Connection(DataSource dataSource) {
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            this.connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            this.connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public java.sql.Connection getConnection() {
        return connection;
    }
}

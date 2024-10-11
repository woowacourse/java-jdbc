package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Connection {
    private final java.sql.Connection connection;
    private int depth;

    public Connection(java.sql.Connection connection) {
        this.connection = connection;
        this.depth = 0;
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

    public boolean isClosed() {
        try {
            return connection.isClosed();
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

    public void close() {
        try {
            depth--;
            if(depth == 0) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void plusDepth() {
        depth++;
    }

    public java.sql.Connection getConnection() {
        return connection;
    }
}

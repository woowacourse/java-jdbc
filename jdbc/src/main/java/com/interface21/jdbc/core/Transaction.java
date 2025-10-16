package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class Transaction {

    private final DataSource dataSource;
    private Connection connection;

    private Transaction(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Transaction init(DataSource dataSource) {
        return new Transaction(dataSource);
    }

    public void begin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            connection = conn;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to rollback transaction", e);
        } finally {
            releaseConnection();
        }
    }

    private void releaseConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close connection", e);
        }
    }
}

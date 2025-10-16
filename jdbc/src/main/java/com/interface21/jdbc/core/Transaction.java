package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Transaction {

    private static Transaction instance;
    private static Connection connection;
    private static DataSource dataSource;

    private Transaction(DataSource dataSource) {
        Transaction.dataSource = dataSource;
    }

    public static Transaction init(DataSource dataSource) {
        if (instance == null) {
            instance = new Transaction(dataSource);
        }
        return instance;
    }

    public void doBegin() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            connection = conn;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to begin transaction", e);
        }
    }

    public void doCommit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to commit transaction", e);
        } finally {
            releaseConnection();
        }
    }

    public void doRollback() {
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

    public Connection getConnection() {
        return connection;
    }
}

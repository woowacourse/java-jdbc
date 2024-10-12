package com.interface21.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;

public class JdbcTransaction {

    private final Connection connection;
    private final H2SQLExceptionTranslator exceptionTranslator;

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
        this.exceptionTranslator = new H2SQLExceptionTranslator();
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            release();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        } finally {
            release();
        }
    }

    private void release() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}

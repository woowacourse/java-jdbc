package com.techcourse.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Transaction implements AutoCloseable {

    private final DataSource dataSource;
    private final Connection connection;

    private Transaction(final DataSource dataSource, final Connection connection) {
        this.dataSource = dataSource;
        this.connection = connection;
    }

    public static Transaction start(final DataSource dataSource) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
            return new Transaction(dataSource, connection);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void setReadOnly(final boolean readOnly) {
        try {
            connection.setReadOnly(readOnly);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void close() {
        DataSourceUtils.releaseConnection(dataSource);
    }
}

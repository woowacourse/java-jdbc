package org.springframework.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;
    private final Connection connection;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.connection = DataSourceUtils.getConnection(dataSource);
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
            this.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
            this.close();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void close() {
        try {
            connection.setAutoCommit(true);
            DataSourceUtils.releaseConnection(connection, dataSource);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
